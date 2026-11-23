package org.opendatamesh.cli.usecases.devops.completetask;

import java.util.List;

/** What complete-task uploads: one log row per file, and the results text (null when there is none). */
record TaskReport(List<String> logs, String results) {
}
