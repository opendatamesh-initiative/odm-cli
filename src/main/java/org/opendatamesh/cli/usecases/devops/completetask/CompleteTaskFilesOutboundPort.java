package org.opendatamesh.cli.usecases.devops.completetask;

interface CompleteTaskFilesOutboundPort {
    ReportFile readLog(String path);

    ResultsFile readResults(String path);
}
