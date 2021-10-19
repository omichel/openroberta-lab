package de.fhg.iais.roberta.worker;

import java.io.File;

import de.fhg.iais.roberta.components.ConfigurationComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.iais.roberta.bean.CompilerSetupBean;
import de.fhg.iais.roberta.components.Project;
import de.fhg.iais.roberta.components.RaspberryPiCommunicator;
import de.fhg.iais.roberta.util.Key;

public class RaspberryPiTransferWorker implements IWorker {
    private static final Logger LOG = LoggerFactory.getLogger(RaspberryPiTransferWorker.class);

    @Override
    public void execute(Project project) {
        CompilerSetupBean compilerWorkflowBean = project.getWorkerResult(CompilerSetupBean.class);
        String tempDir = compilerWorkflowBean.getTempDir();
        ConfigurationComponent conf = project.getConfigurationAst().getConfigurationComponent("RASPBERRYPI");
        String ip = conf.getProperty("IP_ADDRESS");
        int port = Integer.parseInt(conf.getProperty("PORT"));
        String userName = conf.getProperty("USERNAME");
        String pwd = conf.getProperty("PASSWORD");
        RaspberryPiCommunicator communicator = new RaspberryPiCommunicator(ip, port, userName, pwd);
        try {
            String programLocation = tempDir + project.getToken() + File.separator + project.getProgramName() + File.separator + "source";
            communicator.uploadFile(programLocation, project.getProgramName());
            project.setResult(Key.COMPILERWORKFLOW_SUCCESS);
        } catch ( Exception e ) {
            LOG.error("Uploading the generated program to {} failed", ip, e);
            project.setResult(Key.RASPBERRY_PROGRAM_UPLOAD_ERROR);
        }
    }
}
