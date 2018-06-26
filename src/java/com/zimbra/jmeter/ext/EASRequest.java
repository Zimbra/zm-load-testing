package com.zimbra.jmeter.ext;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
//import org.apache.jorphan.logging.LoggingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;

public class EASRequest extends AbstractJavaSamplerClient
       implements Serializable {

  private static final Logger log = LoggerFactory.getLogger(EASRequest.class);

  private String name;

  @Override
  public Arguments getDefaultParameters() {
    Arguments params = new Arguments();
    params.addArgument("Command","Testing");
    params.addArgument("Arguments","Testing");
    return params;
  }

  @Override
  public void setupTest(JavaSamplerContext context) {
    log.info("setupTest info log");
    name = context.getParameter(TestElement.NAME);
  }

  @Override
  public SampleResult runTest(JavaSamplerContext context) {
    log.info("runTest info start");
    SampleResult results = new SampleResult();
    results.setSampleLabel(name);
    results.setSamplerData("SamplerData Hello World");
    results.sampleStart();
    results.setResponseCode("200");
    results.setResponseMessage("OK");
    results.setResponseData("ResponseData Hello World");
    results.setSuccessful(true);
    results.sampleEnd();
    log.info("runTest info end");
    return results;
  }

  @Override
  public void teardownTest(JavaSamplerContext context) {
    log.info("teardownTest info log");
  }
}
