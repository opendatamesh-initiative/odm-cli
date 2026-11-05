# SPDD Analysis: ODM CLI Docker Image (infrastructural slice)

Story **BDMD-5443**, infrastructural level. The functional level, the `odm platform devops` commands, has its own analysis: `spdd/analysis/BDMD-5443-202610261616-[Analysis]-devops-v2-cli-commands.md`. The image carries whatever commands the released jar contains, so it does not depend on the functional decisions.

## Original Business Requirement

### The story

The ODM CLI runs inside the customer's pipeline and keeps DevOps V2 in sync (BDMD-5442). The "DevOps v2 CLI" call of 2026-10-23 split the story into two analyses: a functional one (the commands) and an infrastructural one (a Docker image as an alternative to the executable). The request for this slice: publish a Docker image, so that users can download it and run the CLI.

**Acceptance criterion covered here** (AC7, translated from the story): a Docker image of the CLI can be used in pipelines instead of the executable, with the same commands. The story leaves the image details open ("dettagli dell'immagine da definire").

**Out of scope:** the `platform devops` commands themselves (functional analysis) and the DevOps server (BDMD-5442). The DevOps V2 server analysis (`odm-platform-pp-devops-server/spdd/analysis/BDMD-5442-202610071218-[Analysis]-instrumented-path.md`) already assumes that "the CLI is a Docker image used as a step in the client's pipeline", so the image is the expected way to deliver the instrumented path.

### Decision history

| Rev. | Date | Decisions |
| ---- | ---- | --------- |
| 1 | 2026-10-27 | First draft, written for GitHub Actions and before the devops commands existed. |
| 2 | 2026-11-02 | Updated after the functional slice was implemented and tested with GitLab. GitLab first. New questions: how instrumented jobs, which run the CLI and the build in one job, can use the image (D1); the entrypoint under GitLab's Docker executor (D2); which GitLab executors to support (D3). Configuration keys, command names and the first release are now known from the code. |
| 3 | 2026-11-02 | **Team:** all recommendations accepted (DD-01 to DD-15, D1 to D13). The base image is Amazon Corretto 21, the distribution of the other platform services, instead of Eclipse Temurin (DD-01). |
| 4 | 2026-11-03 | Verified on GitLab with a Docker-executor runner: the image stays non-root (D4). The first Trivy scan found 8 fixable critical issues in the jar (embedded Tomcat and Spring MVC, brought in by the unused `spring-boot-starter-data-rest`) and high issues in other dependencies and OS packages, which would have blocked the release (DD-13). Fixed by removing the starter and the unused `log4j-api` pin, updating Spring Boot to 3.5.16, Jackson to 2.21.8, Log4j to 2.25.5 and Guava to 33.8.0, and applying OS package updates in the image build. The scan then reported no vulnerabilities. |
| 5 | 2026-11-05 | **PR #16 review and team call.** Instrumented tasks use the pipeline id as their run id, like full-control tasks, so `start-task`, the build and `complete-task` run in separate jobs and the CLI image is used as the job image of the CLI jobs (D1 changes from (b) to (c); the copyable bundle is dropped, and the Dockerfile is a single stage on the Corretto image). The image is published like the other ODM services: Docker Hub `opendatamesh/odm-cli`, one `X.Y.Z` tag, `linux/amd64`, plain `docker build`/`docker push`, with no scan, SBOM, provenance or smoke tests (DD-09 to DD-11, DD-13, DD-14). The PR workflow still builds the image without pushing it. Configuration: every key can come from an environment variable, which wins over the file; the packaged registry URL and sample systems are removed (DD-04, D13). |

## Domain Concept Identification

### Existing Concepts (from codebase)

- **Executable distribution (launcher script + jar)**: what users install today. The release workflow (`.github/workflows/cicd.yml`, on `release: created`) checks that the git tag matches the POM version, runs the tests, builds `target/odm-cli-<version>.jar` and attaches it to the GitHub release with the `odm-cli` bash script, whose `VERSION` is pinned by `sed`. The script checks for Java 21, downloads the jar into `~/.odmcli/` (cached through `version.txt`), creates a default `~/.odmcli/application.yml`, downloads the extension jars listed in that file, asks the GitHub API for a newer version on every run, and finally runs `java -Dspring.config.additional-location=file:~/.odmcli/application.yml [-Dloader.path=~/.odmcli/extensions] -jar <jar> "$@"`. The image replaces this whole chain, not only the jar.
- **Release version**: one number shared by the git tag (`v2.0.0`), the POM (`2.0.0`), the jar name and the pinned script. The image tag becomes a fourth carrier. The latest tag is `v1.2.5`; `2.0.0` (with the devops commands) is not released yet.
- **Runnable jar**: a Spring Boot fat jar with `layout ZIP`, so it starts through Spring Boot's `PropertiesLauncher`. That is what makes `loader.path` (and the `LOADER_PATH` environment variable) work for extensions. Any packaging that does not start the jar through this launcher loses extension loading.
- **CLI configuration**: `OdmCliConfiguration` (`@ConfigurationProperties(prefix = "cli")`) is bound from the packaged `src/main/resources/application.yml` plus the external file passed as `spring.config.additional-location`. The keys a pipeline needs:
  - `cli.platform.services.registry.url` and `cli.platform.services.devops.url`, the service URLs;
  - `cli.platform.services.*.headers` and `oauth2`, for authentication where the platform requires it;
  - `cli.platform.devops.*`, the polling and retry tuning (`pollInterval`, `activityApprovalTimeout`, `taskApprovalTimeout`, `runIdLookupAttempts`, `runIdLookupInterval`, `conflictRetries`, `conflictRetryInterval`), with packaged defaults;
  - `cli.cliConfiguration.interactive`, packaged as `true`.
- **Environment-variable configuration (relaxed binding)**: Spring's relaxed binding reaches `OdmCliConfiguration`. It was verified with `CLI_PLATFORM_SERVICES_REGISTRY_URL`, and the GitLab end-to-end tests configured the CLI only through `CLI_PLATFORM_SERVICES_REGISTRY_URL` and `CLI_PLATFORM_SERVICES_DEVOPS_URL` in the CI file. Lists (`systems`, `headers`) need indexed variables and replace the whole packaged list.
- **GitLab usage of the devops commands** (`docs/cmd-platform-devops-start-task.md`, `docs/cmd-platform-devops-complete-task.md`, end-to-end report `agents-reports/BDMD-5443/e2e-local-tests.md`):
  - **Instrumented:** one job runs `start-task --provider-run-id "$CI_JOB_ID"`, writes a marker file, runs the user's build (writing `build.log` and `results.json`), and in `after_script`, only if the marker exists, maps `CI_JOB_STATUS` to a status and runs `complete-task`. The CLI and the user's build tools run **in the same job, so in the same image**.
  - **Full control:** a job writes `results.json` and runs `complete-task --provider-run-id "$CI_PIPELINE_ID" --results-only`. It only needs the CLI.
- **`config` command**: reads and edits the external configuration file. Writing requires `spring.config.additional-location` (otherwise "The external configuration file is not present."); printing reads `SPRING_APPLICATION_JSON` or the packaged `application.yml`, not the external file nor environment variables. In a container, edits are lost when it exits.
- **Interactive mode**: the root `--interactive/-i` option, defaulting to `cli.cliConfiguration.interactive` (packaged `true`). When a value is missing and interactive is on, executors call `System.console().readLine(...)`, and on Java 21 `System.console()` is `null` without a terminal, so the result is a `NullPointerException`. The devops commands never prompt (functional DD-10); the other commands (`local init`, `local import`, `platform registry validate`) still can.
- **Working-directory-relative defaults**: commands resolve paths against the working directory (`platform registry validate` without `-f` reads `./data-product-descriptor.json`; `complete-task` reads `--logs-file` and `--results-file`).
- **Extensions**: `ExtensionsLoader` finds `ImporterExtension` implementations with `ServiceLoader`; only `local import` uses them. Extension jars reach the classpath only through `loader.path`.
- **Exit codes**: `0` success, `1` command failure, `2` invalid usage (README "Exit codes"). Pipelines rely on them to fail a step.
- **Logging**: `logback-spring.xml` writes to the console, errors with ANSI colours. On failure the CLI prints a full Java stack trace after the error line, which is noisy in pipeline logs (end-to-end report).
- **Release notes**: `agents-reports/2.0.0-user-impacting-changes.md` lists the Docker image as planned and not yet in the docs.

### New Concepts Required

- **CLI container image**: a published, versioned OCI image with a Java 21 runtime, the released jar and an entrypoint. It is a second distribution of the same release artifact, not a new build of the CLI.
- **`odm-cli` command in the image**: an `odm-cli` launcher on `PATH`, so that a GitLab job script (or a GitHub container job) calls `odm-cli …` exactly like the executable. It starts the jar with the external configuration location and the extensions directory, and passes arguments and exit code through. It drops the Java check, the downloads, the cache and the update check.
- **Shell-compatible entrypoint**: the image's `ENTRYPOINT`, which runs a shell command when the container is started with one (GitLab's Docker executor does this for every job) and runs `odm-cli` with the arguments otherwise (`docker run <image> <args>`).
- ~~**Copyable CLI bundle**~~ _(dropped in rev. 5)_: a self-contained `/opt/odm-cli` to copy into the users' build images. Not needed once instrumented tasks run the CLI in its own jobs (D1).
- **Image repository and tags**: the registry location and the tag scheme that maps to release versions.
- **Image build-and-publish step**: a stage of the release workflow that builds the image from the released jar and pushes it, plus a build-only variant in the PR workflow.
- **Container configuration contract**: how the CLI is configured in the image: GitLab CI/CD variables mapped to environment variables (primary), an optional mounted configuration file at a known path, and image defaults that suit CI.
- **Image tests**: smoke tests on the built image, plus a GitLab pipeline that uses it.
- **Usage documentation**: a README section and a `docs/` page with GitLab examples first.

### Key Business Rules

- **Same release, same bytes**: the image of version X contains exactly the jar attached to GitHub release X. Image tag, git tag, POM version and jar name carry the same number.
- **Same commands**: `odm-cli <args>` inside the image, and `docker run <image> <args>`, behave exactly like the executable and return the same exit code. The image adds and hides no commands.
- **Exit code is the contract with the pipeline**: nothing in the entrypoint or launcher may swallow or remap it.
- **No secrets or environment-specific values in the image**: URLs, credentials and OAuth2 settings are supplied at run time, as the `CONFIGURATION.md` norm requires.
- **Never hang in a pipeline**: a command must fail with a clear message rather than wait for input that cannot arrive.
- **No network at startup**: the image does not contact GitHub (version check, downloads). Only the configured ODM services are contacted.
- **Pinned by default**: pipelines reference an immutable version tag; moving tags are a convenience, never the documented default.
- **Published only for a verified release**: the gate that protects the jar (tag/POM match, tests) protects the image.

## Strategic Approach

### Solution Direction

- Package the **already-built release jar** on a **Java 21 JRE base** with a shell, add an `odm-cli` launcher on `PATH` and a shell-compatible entrypoint, and publish the image from the **existing release workflow**, tagged with the release version.
- In GitLab, the image is used as a **job image** for jobs that only call the CLI (full-control results upload, `registry validate`). For instrumented jobs, which also run the user's build, the way to get the CLI into the build image is the main open point (D1).
- Configuration goes through **environment variables** (GitLab CI/CD variables, masked for secrets), with an optional mounted `application.yml` for list-shaped configuration, and image defaults tuned for CI.
- Reuse the existing conventions: the release gate in `cicd.yml`, `PropertiesLauncher` and `loader.path`, the exit codes and the configuration norm. Changes to the CLI code are avoided or kept small and generic.

### Key Design Decisions

- **DD-01: Base image and runtime** _(team, rev. 3; rev. 5: the copied bundle is gone, so the image is built `FROM amazoncorretto:21-al2023-headless` directly, and glibc is kept for GitHub container jobs)_: the other platform services (`odm-platform-pp-devops-server`, `odm-platform-pp-registry-server`, `odm-platform-service-template` and others) are built on **Amazon Corretto 21**, so the CLI uses the same distribution. The services use the `21-alpine-jdk` variant; the CLI needs a different variant because GitLab's Docker executor runs every job script through a shell inside the job image, the copyable bundle (D1) must run in the users' build images, which are mostly glibc-based, and GitHub container jobs need glibc for JavaScript actions. A musl (Alpine) runtime copied into a glibc image does not start, and distroless has no shell. → **Decision:** `amazoncorretto:21-al2023-headless` (Corretto 21 on Amazon Linux 2023: glibc, a shell, no GUI libraries, multi-arch). Alpine and distroless are out. A `jlink` runtime is reconsidered only if the copyable bundle turns out too large.
- **DD-02: What goes into the image**: options are (a) the jar built by the release workflow, (b) the release asset downloaded inside the Docker build, (c) a multi-stage build from source, (d) Buildpacks or Jib. (c) needs GitHub Packages credentials in the build and produces different bytes; (d) gives no control over the entrypoint, and Jib bypasses `PropertiesLauncher`, which drops `loader.path`. → **Decision:** (a), the jar from `build-and-release-jar` handed to the image job, with a plain Dockerfile in the repository. The jar is copied under a version-independent name.
- **DD-03: Launcher and entrypoint**: the bash script is not reused (see Alternatives). → **Decision:**
  - an `odm-cli` launcher on `PATH` that `exec`s `java -jar` through `PropertiesLauncher`, with the configuration location and the extensions path (`SPRING_CONFIG_ADDITIONAL_LOCATION`, `LOADER_PATH`), and arguments passed unchanged;
  - an `ENTRYPOINT` that runs the given command when the first argument is an executable on `PATH` (`sh`, `bash`, `odm-cli`), and runs `odm-cli "$@"` otherwise. GitLab jobs then work without `entrypoint: [""]`, and `docker run <image> <args>` still works.

  In `docker run` the JVM is the container's main process, so signals reach it. In a GitLab job it is a child of the job shell.
- **DD-04: Configuration contract** _(confirmed in rev. 5: every key can come from an environment variable or the configuration file, and the variable wins; verified on the jar and locked by `OdmCliConfigurationEnvironmentTest`, list and `oauth2` keys included; documented in the README for every distribution)_: → **Decision:** environment variables as the documented primary path, with a table of the keys a pipeline sets: `CLI_PLATFORM_SERVICES_REGISTRY_URL`, `CLI_PLATFORM_SERVICES_DEVOPS_URL`, and the authentication keys if the platform requires them. In GitLab they are CI/CD variables, with credentials masked and never in the CI file. In addition, a fixed optional configuration file path in the image, passed as an optional `spring.config.additional-location`, so users can mount or write one (for example from a GitLab file variable). Nothing environment-specific is baked into the image.
- **DD-05: Non-interactive by default**: → **Decision:** the image sets `CLI_CLICONFIGURATION_INTERACTIVE=false`, overridable for `docker run -it`. The devops commands never prompt already; this protects the other commands. The underlying NPE is a CLI issue (D8).
- **DD-06: Workspace and file access**: in GitLab the job runs in the project checkout under `/builds/…`, so relative paths work as with the executable. For `docker run`, the user mounts the directory and sets the working directory (`-v "$PWD":/workspace -w /workspace`). → **Decision:** `WORKDIR /workspace`, the `docker run` form documented with `-v` and `-w`, and a note that `~` and absolute paths refer to the container.
- **DD-07: Image user**: GitLab's helper container clones the project into `/builds` as root, and instrumented jobs write `build.log`, the marker file and `results.json` there. A non-root user is safer; root is the most compatible. → **Decision:** a dedicated non-root user with a writable home, kept only if a GitLab job with the Docker executor can write in the checkout (D4). Otherwise root, with the reason recorded. Document `--user "$(id -u):$(id -g)"` for local `docker run`.
- **DD-08: Extensions**: → **Decision:** none bundled and no runtime download. An empty extensions directory on `loader.path`; users build a derived image or mount jars. Only `local import` uses extensions, and it is not a pipeline command (D7).
- **DD-09: Registry** _(revised in rev. 5)_: → **Decision:** Docker Hub, `opendatamesh/odm-cli`, like the other ODM services, logging in with the `DOCKER_USERNAME`/`DOCKER_PASSWORD` secrets. GHCR (rev. 1 to 4) is dropped (D5).
- **DD-10: Tags** _(revised in rev. 5)_: → **Decision:** publish only from the release workflow, after the gate, with one immutable tag `X.Y.Z` (no `v` prefix), as the other ODM services do. No moving tags. OCI labels for version, source and revision. No images from `main`; PRs build without pushing (D6).
- **DD-11: Multi-architecture** _(revised in rev. 5)_: → **Decision:** `linux/amd64` only, the platform of a plain `docker build` on the GitHub runner, as the other ODM services. arm64 hosts run it under emulation.
- **DD-12: Logs and output**: → **Decision:** the same output as the executable. Quieter pipeline output, including the stack-trace noise, is a CLI-wide decision, not an image one (D10).
- **DD-13: Supply-chain security** _(revised in rev. 5, PR #16 review)_: → **Decision:** no scan, SBOM, provenance or signing in the release workflow, as the other ODM services. The image installs the OS package updates at build time (`dnf -y upgrade`), and the jar keeps the dependency fixes of rev. 4 (D11).
- **DD-14: Testing the image** _(revised in rev. 5, PR #16 review: the smoke tests and `docker/smoke-test.sh` are removed; the PR workflow only builds the image, and the image is verified manually on GitLab with `gitLabDp`. The list below is the rev. 3 decision)_: → **Decision:**
  - **Smoke tests before push:** `odm-cli --version` prints the release version; `--help` exits `0`; an unknown option exits `2`; an unreachable registry exits `1` with a clear message; an environment variable is applied; the image runs as the expected user; `sh -c 'odm-cli --version'` works (the GitLab form).
  - **GitLab pipeline:** the full-control and instrumented jobs of the `gitLabDp` test project, run on the local runner with the built image, in the form D1 chooses.
  - **GitHub:** `docker://` and `container:` checks are added when GitHub is targeted.
- **DD-15: Documentation**: → **Decision:** a README "Docker" subsection and a `docs/` page covering the pull, GitLab job examples (full control with the image as job image; instrumented in the form D1 chooses), `docker run` with the workspace mount, the configuration table, the non-interactive default, user notes, extensions and exit codes. GitHub examples come later. Update `agents-reports/2.0.0-user-impacting-changes.md`.

### Alternatives Considered

- **Reuse the bash launcher as the entrypoint**: rejected. It downloads the jar and extensions at run time, calls the GitHub API on every run (rate limits on shared CI addresses), needs `wget`, and its `grep "url:"` parsing would also match the service URLs and try to download them as extensions.
- **Native image (GraalVM)**: rejected for now. Spring Boot, picocli and `ServiceLoader` extensions from `loader.path` do not fit a closed-world build, and it would be a second build to test.
- **Build the jar inside the Docker build**: rejected. It needs GitHub Packages credentials and produces a different artifact.
- **Jib or Buildpacks**: rejected as the primary path. Jib bypasses `PropertiesLauncher`; Buildpacks fix the user and entrypoint.
- **Entrypoint that is the CLI itself**: rejected. GitLab's Docker executor would pass the job's shell command to the CLI, so every job would need `entrypoint: [""]`.
- **Split an instrumented task into separate start, build and complete jobs**: rejected in rev. 2, **chosen in rev. 5** (D1). With the pipeline id as the run id, every job of the pipeline shares it; the status comes from `on_success`/`on_failure` jobs (GitLab) or `needs.<job>.result` (GitHub). A cancelled GitLab pipeline still leaves the task `RUNNING`: documented with the manual recovery, and a DevOps webhook receiver or stale-task timeout is a server follow-up.
- **Copy the CLI bundle into the user's build image** (`COPY --from=… /opt/odm-cli`): chosen in rev. 2, dropped in rev. 5. It required a glibc build image and a change to the user's image.
- **`docker run` from inside the build job**: rejected. It needs Docker-in-Docker and privileged runners.
- **A GitLab CI/CD Catalog component or a GitHub Action**: deferred (D12).
- **Images from every push to `main`**: deferred.

## Risk & Gap Analysis

### Settled by the code

- **Command names and placement**: `platform devops start-task` and `complete-task`, final.
- **Pipeline configuration keys**: listed in DD-04; the GitLab end-to-end tests needed only the two service URLs.
- **First release with the image**: `2.0.0`, which is not released yet and carries the devops commands. No backfill of 1.x images.
- **Prompting in devops commands**: they never prompt (functional DD-10).

### Decisions on the open questions

All the recommendations were accepted by the team in rev. 3.

| # | Question | Decision |
|---|----------|----------------|
| D1 | **How do instrumented GitLab jobs use the image?** The job runs `start-task`, the user's build and `complete-task` in one job, and a GitLab job has one image: the user's build image. Options: (a) the image is used only for CLI-only jobs, and instrumented users keep the executable; (b) the image also ships a self-contained `/opt/odm-cli` (Java runtime, jar, launcher) that users copy into their build image with `COPY --from=ghcr.io/…/odm-cli:<version> /opt/odm-cli /opt/odm-cli`, which needs a glibc-based build image; (c) separate start, build and complete jobs; (d) `docker run` from the build job. | **Rev. 5:** (c). Instrumented tasks use the pipeline id as their run id (`$CI_PIPELINE_ID`, `github.run_id`), so `start-task` and `complete-task` run in their own jobs with the CLI image, and the build job keeps the user's image. DevOps already prevents two running tasks of one activity, so the shared id is safe; one pipeline must drive one activity. (Rev. 3: (b), the copyable bundle.) |
| D2 | **Entrypoint under GitLab.** Shell-compatible entrypoint (DD-03), or a plain `odm-cli` entrypoint with `entrypoint: [""]` documented for every GitLab job? | Shell-compatible entrypoint, so GitLab jobs need no override and `docker run <image> <args>` still works. |
| D3 | **Which GitLab executors do customers use?** Docker, Kubernetes, or shell. Shell-executor users would use the executable, and the Kubernetes executor treats the image's entrypoint and file permissions differently from the Docker executor. | Support and test the Docker executor; document the Kubernetes executor after a test; shell executor users keep the executable. |
| D4 | **Image user.** Non-root, or root? It depends on whether a non-root user can write in the GitLab checkout (`build.log`, marker, `results.json`) on the executors of D3. | Non-root if the GitLab test passes on the Docker executor; root otherwise, with the reason documented. **Verified 2026-11-03:** both GitLab jobs wrote in the checkout as UID 10001 on the Docker executor, so the image stays non-root (`agents-reports/BDMD-5443/e2e-local-tests.md`, test 3). |
| D5 | **Registry and name.** GHCR, Docker Hub, or both? Who owns the package visibility setting (GHCR packages may start private)? | **Rev. 5:** Docker Hub, `opendatamesh/odm-cli`, requested in the PR #16 review to align with the other ODM services. Customers in closed networks mirror the immutable tags. |
| D6 | **Tags.** Which moving tags, are prereleases published, are images built from `main`? | **Rev. 5:** `X.Y.Z` only, as the other ODM services; no `main` images. |
| D7 | **Extensions.** Any pipeline use case that needs extensions in the image? | No bundling, no runtime download; an extensions directory on `loader.path`, derived images or mounts. |
| D8 | **No-console NPE.** Fix the `System.console()` NPE in the CLI with a generic "no console, use `--interactive=false`" error, or rely on the image default? | Image default in this slice; the CLI guard as a separate small fix. |
| D9 | **`config` in the container.** Should `config --set/--unset` write into the optional config file path, and should `config --print` reflect environment variables? | Point the optional location at the config file path so `config` behaves as on a workstation within one run, and document that edits are lost unless the file is mounted. The `--print` behaviour is a pre-existing CLI issue, tracked separately. |
| D10 | **Pipeline output.** Should the stack trace printed after a failure, and the startup lines, be removed before the image ships? | Same output as the executable in the image. Fix the stack-trace noise in the CLI for all distributions, ideally before 2.0.0. |
| D11 | **Supply chain.** Mandatory scan gate, SBOM, signing, scheduled rebuilds for base-image CVEs? | **Rev. 5:** none of them, as the other ODM services (PR #16 review). Rebuild on the next CLI release. |
| D12 | **Packaged pipeline step.** Publish a GitLab CI/CD Catalog component (an instrumented job template) or, later, a GitHub Action? | Not in this slice. A Catalog component is the natural follow-up once D1 is settled and the first users have tried the examples. |
| D13 | **Packaged sample values.** `src/main/resources/application.yml` ships `cli.systems` with `root/root`, `template.owner` `testId`/`456123` and registry `http://localhost:8001`, which become visible defaults of a public image. | **Rev. 5:** the registry URL and the sample `cli.systems` are removed; the missing-URL errors name the environment variable and the key. The `template.owner` and `env.custom` samples stay for now: `local init` reads the owner without a null check. |

### Edge Cases

- **Relative and default paths**: in a GitLab job they resolve against the checkout. With `docker run`, forgetting `-w` makes the CLI look in `WORKDIR` and fail with "Descriptor root path does not exist".
- **`~` and absolute host paths**: `~` is the container user's home, and host paths do not exist in the container.
- **Files written in the workspace**: instrumented jobs and `local init`/`local import` write files. A mismatched UID fails with permission errors; root leaves root-owned files on self-hosted runners and local mounts.
- **No TTY**: if interactive is forced back to `true` without `-it`, any prompt ends with an NPE.
- **Cancellation**: a cancelled GitLab job stops the container; `after_script` decides whether `complete-task` records `CANCELED`. With `docker run`, the JVM is the main process and must shut down cleanly on SIGTERM during long polls.
- **Environment variable names for camelCase keys**: `cli.cliConfiguration.interactive` maps to `CLI_CLICONFIGURATION_INTERACTIVE`. Scalar keys are verified; list keys and the `oauth2` fields need a test.
- **List replacement**: `CLI_SYSTEMS_0_…` replaces the packaged list; a mounted file plus variables for the same list combine in non-obvious ways.
- **Proxies and custom CAs**: corporate runners often need a proxy or a private CA. The image must accept JVM options (`JAVA_TOOL_OPTIONS`) and document a derived image for trusted certificates.
- **Architecture mismatch**: arm64 runners pulling an amd64-only image; multi-arch avoids it.

### Technical Risks

- **Cancelled GitLab pipeline** (rev. 5): with separate jobs, the close jobs do not run and the instrumented task stays `RUNNING`. Mitigation: documented manual close (`complete-task --status CANCELED`) or `--force-start`; a DevOps webhook receiver or stale-task timeout is a server follow-up.
- **GitLab executors**: the entrypoint and file-permission behaviour differ between the Docker and Kubernetes executors. Mitigation: test on the Docker executor; document the others after testing (D3).
- **Base image choice**: distroless would break GitLab jobs (no shell), and the Alpine variant used by the services would break GitHub container jobs (musl). Mitigation: the Corretto Amazon Linux variant (DD-01).
- **Extension loading depends on the jar launcher**: changing the packaging drops `loader.path`. Mitigation: start through `PropertiesLauncher` (DD-02, DD-03).
- **Release workflow coupling**: a failure after the release assets are attached leaves a release without an image. Mitigation: make the image job re-runnable and idempotent on tags.
- **CI permissions**: pushing to GHCR needs `packages: write` on the image job only. Mitigation: least-privilege permissions per job.
- **Startup time**: each CLI call starts a JVM (about 1.5 s for Spring), plus the pull. Acceptable for two calls per job; Spring Boot CDS/AOT is a later optimisation.
- **Image freshness**: the JRE base accumulates CVEs between releases. Mitigation: scanning (DD-13); periodic rebuilds are a later decision (D11).
- **Configuration drift between distributions**: the launcher creates `~/.odmcli/application.yml` with `extensions: urls: []`, while the README documents a list of `name`/`url` entries. The image documents its own contract (DD-04) and copies neither.

### Acceptance Criteria Coverage

| AC# | Description | Addressable? | Gaps/Notes |
|-----|-------------|--------------|------------|
| 1–6 | The devops commands and their placement | Out of scope | Functional slice, implemented. The image carries them and must propagate their exit codes (DD-03, DD-14). |
| 7 | A Docker image of the CLI usable in pipelines instead of the executable, with the same commands | Yes | "Same commands" comes from packaging the released jar (DD-02) behind an argument-transparent launcher (DD-03). "Usable in pipelines" for GitLab depends on D1 (instrumented jobs), D2 (entrypoint), D3 (executors) and D4 (user). The image details are answered by DD-01 to DD-15. |
