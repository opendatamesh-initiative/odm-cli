package org.opendatamesh.cli.commands;

import org.opendatamesh.cli.OdmCliApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {OdmCliApplication.class})
public class OdmCliCommandIT {
    @Autowired
    protected OdmCliRootCommandBuilder rootCommand;

}
