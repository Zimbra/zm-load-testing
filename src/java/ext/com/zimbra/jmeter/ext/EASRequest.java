package com.zimbra.jmeter.ext;

// imports for basic jmeter extension
import java.io.Serializable;
import java.util.Properties;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// imports for EAS support
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import com.zimbra.zimbrasync.wbxml.BinarySerializer;
import com.zimbra.zimbrasync.wbxml.BinaryParser;

public class EASRequest extends AbstractJavaSamplerClient
       implements Serializable {

  private static final Logger log = LoggerFactory.getLogger(EASRequest.class);

  private String contenttype;
  private String name;
  private String command;
  JMeterVariables vars; 
  Properties props;

  // highlight required argument in interface for this to work
  @Override
  public Arguments getDefaultParameters() {
    Arguments params = new Arguments();
    params.addArgument("Command","");
    return params;
  }

  // setup basic information for test
  @Override
  public void setupTest(JavaSamplerContext context) {
    //log.info("setupTest info log");
    command = context.getParameter("Command");
    name    = context.getParameter(TestElement.NAME);
  }

  // run the Test
  @Override
  public SampleResult runTest(JavaSamplerContext context) {
    vars = context.getJMeterVariables(); 
    props = context.getJMeterProperties(); 
    contenttype = "application/vnd.ms-sync.wbxml";

    SampleResult results = new SampleResult();
    results.setSampleLabel(name);

    //log.info("runTest info start");
    results.sampleStart();

    if (command.equals("FolderSync")) {
      folderSync(context,results);
    } else if (command.equals("FolderCreate")) {
      folderCreate(context,results);
    } else if (command.equals("FolderDelete")) {
      folderDelete(context,results);
    } else if (command.equals("Search")) {
      search(context,results);
    } else if (command.equals("SendMail")) {
      sendMail(context,results);
    } else if (command.equals("Sync")) {
      sync(context,results);
    } else {
      results.setResponseData("'"+command+"' not supported",null);
      results.setResponseCode("400");
      results.setResponseMessage("Bad Request");
      results.setSuccessful(false);
    }

    results.sampleEnd();
    //log.info("runTest info end");

    return results;
  }

  // Not currently used just a holder for now
  @Override
  public void teardownTest(JavaSamplerContext context) {
    //log.info("teardownTest info log");
  }

  // generate the FolderSync command then call runCommand
  private void folderSync(JavaSamplerContext context,SampleResult results) {
    try {
      //Generate WBXML Request Body
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BinarySerializer bs = new BinarySerializer(bao,false);
      bs.openTag(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                 BinarySerializer.FOLDERHIERARCHY_FOLDERSYNC);
      bs.textElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                     BinarySerializer.FOLDERHIERARCHY_SYNCKEY,
                     vars.get("FOLDERSYNCKEY"));
      bs.closeTag();
  
      //Store request
      results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
        
      //Add WBXML Body to request
      HttpEntity entity = new ByteArrayEntity(bao.toByteArray());

      runCommand(results,entity);
    } catch (Exception e) {
      log.error(command+": "+e.toString());
      results.setSuccessful(false);
    }
  }

  // generate the FolderCreate command then call runCommand
  private void folderCreate(JavaSamplerContext context,SampleResult results) {
    try {
      //Generate WBXML Request Body
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BinarySerializer bs = new BinarySerializer(bao,false);
      bs.openTag(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                 BinarySerializer.FOLDERHIERARCHY_FOLDERCREATE);
      bs.textElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                     BinarySerializer.FOLDERHIERARCHY_SYNCKEY,
                     vars.get("FOLDERSYNCKEY"));
      bs.integerElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                        BinarySerializer.FOLDERHIERARCHY_PARENTID,0);
      bs.textElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                     BinarySerializer.FOLDERHIERARCHY_DISPLAYNAME,
                     "ActiveSyncTestDir");
      bs.integerElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                        BinarySerializer.FOLDERHIERARCHY_TYPE,12);
      bs.closeTag();
  
      //Store request
      results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
        
      //Add WBXML Body to request
      HttpEntity entity = new ByteArrayEntity(bao.toByteArray());
      runCommand(results,entity);
    } catch (Exception e) {
      log.error(command+": "+e.toString());
      results.setSuccessful(false);
    }
  }

  // generate the FolderDelete command then call runCommand
  private void folderDelete(JavaSamplerContext context,SampleResult results) {
    try {
      //log.info(command+": Key="+vars.get("FOLDERSYNCKEY")+
      //         " Folder="+vars.get("FOLDERID"));
      //Generate WBXML Request Body
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BinarySerializer bs = new BinarySerializer(bao,false);
      bs.openTag(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                 BinarySerializer.FOLDERHIERARCHY_FOLDERDELETE);
      bs.textElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                     BinarySerializer.FOLDERHIERARCHY_SYNCKEY,
                     vars.get("FOLDERSYNCKEY"));
      bs.textElement(BinarySerializer.NAMESPACE_FOLDERHIERARCHY,
                     BinarySerializer.FOLDERHIERARCHY_SERVERID,
                     vars.get("FOLDERID"));
      bs.closeTag();
  
      //Store request
      results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
        
      //Add WBXML Body to request
      HttpEntity entity = new ByteArrayEntity(bao.toByteArray());
      runCommand(results,entity);
    } catch (Exception e) {
      log.error(command+": "+e.toString());
      results.setSuccessful(false);
    }
  }

  // generate the SendMail command then call runCommand
  private void sendMail(JavaSamplerContext context,SampleResult results) {
    // Should get changed so we pull message from file or better generate
    // using propsed message builder outlined "Zimbra JMeter" document
    String message = "";
    message += "From: <"+vars.get("USER")+"@"+props.get("HTTP.domain")+">\n";
    message += "To: <"+vars.get("USER")+"@"+props.get("HTTP.domain")+">\n";
    message += "Subject: From Active Sync\n";
    message += "MIME-Version: 1.0\n";
    message += "Content-Type:  text/plain; charset=utf-8\n";
    message += "Content-Transfer-Encoding: 7bit\n\n";
    message += "This is the test message.\n";

    if (props.getProperty("EAS.version").equals(14.0) ||
        props.getProperty("EAS.version").equals(14.1)) {
      // send message using wbxml
      try {
        //Generate WBXML Request Body
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        BinarySerializer bs = new BinarySerializer(bao,false);
        bs.openTag(BinarySerializer.NAMESPACE_COMPOSEMAIL,
                   BinarySerializer.COMPOSEMAIL_SENDMAIL);
        bs.textElement(BinarySerializer.NAMESPACE_COMPOSEMAIL,
                       BinarySerializer.COMPOSEMAIL_CLIENTID,
                       UUID.randomUUID().toString());
        bs.opaqueElement(BinarySerializer.NAMESPACE_COMPOSEMAIL,
                         BinarySerializer.COMPOSEMAIL_MIME,message);
        bs.closeTag();
    
        //Store request
        results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
          
        //Add WBXML Body to request
        HttpEntity entity = new ByteArrayEntity(bao.toByteArray());
        runCommand(results,entity);
      } catch (Exception e) {
        log.error(command+" "+props.getProperty("EAS.version")+": "+
                  e.toString());
        results.setSuccessful(false);
      }
    } else {
      // send message as content
      try {
        contenttype="message/rfc822";
    
        //Store request
        results.setSamplerData(message);
          
        //Add message to request
        HttpEntity entity = new StringEntity(message);
        runCommand(results,entity);
      } catch (Exception e) {
        log.error(command+" "+props.getProperty("EAS.version")+": "+
                  e.toString());
        results.setSuccessful(false);
      }
    }
  }

  // generate the Search command then call runCommand
  private void search(JavaSamplerContext context,SampleResult results) {
    try {
      //Generate WBXML Request Body
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BinarySerializer bs = new BinarySerializer(bao,false);
      bs.openTag(BinarySerializer.NAMESPACE_SEARCH,
                 BinarySerializer.SEARCH_SEARCH);
      bs.openTag(BinarySerializer.NAMESPACE_SEARCH,
                 BinarySerializer.SEARCH_STORE);
      bs.textElement(BinarySerializer.NAMESPACE_SEARCH,
                     BinarySerializer.SEARCH_NAME,"Mailbox");
      bs.openTag(BinarySerializer.NAMESPACE_SEARCH,
                 BinarySerializer.SEARCH_QUERY);
      bs.openTag(BinarySerializer.NAMESPACE_SEARCH,
                 BinarySerializer.SEARCH_AND);
      bs.textElement(BinarySerializer.NAMESPACE_SEARCH,
                     BinarySerializer.SEARCH_FREETEXT,"test");
      bs.closeTag();
      bs.closeTag();
      bs.openTag(BinarySerializer.NAMESPACE_SEARCH,
                 BinarySerializer.SEARCH_OPTIONS);
      bs.textElement(BinarySerializer.NAMESPACE_SEARCH,
                     BinarySerializer.SEARCH_RANGE,"0-9");
      bs.closeTag();
      bs.closeTag();
      bs.closeTag();
  
      //Store request
      results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
        
      //Add WBXML Body to request
      HttpEntity entity = new ByteArrayEntity(bao.toByteArray());
      runCommand(results,entity);
    } catch (Exception e) {
      log.error(command+": "+e.toString());
      results.setSuccessful(false);
    }
  }

  // generate the Sync command then call runCommand
  private void sync(JavaSamplerContext context,SampleResult results) {
    try {
      //Generate WBXML Request Body
      ByteArrayOutputStream bao = new ByteArrayOutputStream();
      BinarySerializer bs = new BinarySerializer(bao,false);
      bs.openTag(BinarySerializer.NAMESPACE_AIRSYNC,
                 BinarySerializer.AIRSYNC_SYNC);
      bs.openTag(BinarySerializer.NAMESPACE_AIRSYNC,
                 BinarySerializer.AIRSYNC_COLLECTIONS);
      bs.openTag(BinarySerializer.NAMESPACE_AIRSYNC,
                 BinarySerializer.AIRSYNC_COLLECTION);
      bs.textElement(BinarySerializer.NAMESPACE_AIRSYNC,
                     BinarySerializer.AIRSYNC_SYNCKEY,vars.get("SYNCSYNCKEY"));
      bs.integerElement(BinarySerializer.NAMESPACE_AIRSYNC,
                        BinarySerializer.AIRSYNC_COLLECTIONID,2);
      bs.closeTag();
      bs.closeTag();
      bs.closeTag();
  
      //Store request
      results.setSamplerData(wbxmlToXMLString(bao.toByteArray()));
        
      //Add WBXML Body to request
      HttpEntity entity = new ByteArrayEntity(bao.toByteArray());
      runCommand(results,entity);
    } catch (Exception e) {
      log.error(command+": "+e.toString());
      results.setSuccessful(false);
    }
  }

  // Combine common code to run the commands in one place
  private void runCommand(SampleResult results,HttpEntity entity)
               throws Exception {
    //Setup HTTP Request
    HttpPost hp = getHttpPost();
      
    //Add WBXML Body to request
    hp.setEntity(entity);
    
    HttpResponse hr = getHttpResponse(hp);
    
    //Process reponse for reporting results in jmeter
    results.setResponseCode(Integer.toString(
                            hr.getStatusLine().getStatusCode()));
    results.setResponseMessage(hr.getStatusLine().getReasonPhrase());
    
    //Convert WBXML to readable form for jmeter
    if (hr.getEntity().getContentLength() > 0) {
      String result=wbxmlToXMLString(EntityUtils.toByteArray(hr.getEntity()));
      results.setResponseData(result,null);
    }

    results.setSuccessful(true);
  }

  // initialize HttpPost request with comman values shared by all commands
  private HttpPost getHttpPost() {
    HttpPost hp = new HttpPost(vars.get("URL")+"?Cmd="+vars.get("COMMAND")+
                  "&User="+vars.get("USER")+
                  "&DeviceId=testing&DeviceType=jmeter");
    String auth = vars.get("USER")+"@"+props.get("HTTP.domain")+":"+
                  vars.get("PASS");
    byte[] encodedAuth = Base64.encodeBase64(
                  auth.getBytes(StandardCharsets.ISO_8859_1));
    String authHeader = "Basic "+new String(encodedAuth);
    hp.setHeader(HttpHeaders.AUTHORIZATION,authHeader);
    hp.setHeader("Content-Type",contenttype);
    hp.setHeader("MS-ASProtocolVersion",
                 props.getProperty("EAS.version","12.1"));
    return hp;
  }

  // process http request and get response consistantly for all commands
  private HttpResponse getHttpResponse(HttpPost hp) throws Exception {
    //Disable SSL validation/verification
    SSLContextBuilder builder = new SSLContextBuilder();
    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
              builder.build(),NoopHostnameVerifier.INSTANCE);
      
    //Create http connection and send request
    CloseableHttpClient dhc = HttpClients.custom().
                                 setSSLSocketFactory(sslsf).build();
    HttpResponse hr = dhc.execute(hp);
    return hr;
  }

  // convert wbxml to xml string
  private String wbxmlToXMLString(byte[] wbxml) throws Exception {
    String result="";
    try {
      //BinaryParser bp = new BinaryParser(new ByteArrayInputStream(
      //                   EntityUtils.toByteArray(hr.getEntity())),false);
      BinaryParser bp = new BinaryParser(new ByteArrayInputStream(wbxml),false);
      while (bp.next() != BinaryParser.END_DOCUMENT) {
        if (bp.getEventType() == BinaryParser.START_DOCUMENT) {
          result+="<"+bp.getName()+">";
        } else if (bp.getEventType() == BinaryParser.END_DOCUMENT) {
          result+="</"+bp.getName()+">"; 	
        } else if (bp.getEventType() == BinaryParser.START_TAG) {
          result+="<"+bp.getName()+">"; 	
        } else if (bp.getEventType() == BinaryParser.END_TAG) {
          result+="</"+bp.getName()+">"; 	
        } else if (bp.getEventType() == BinaryParser.TEXT) {
          result+=bp.getText(); 	
        }
      }
    } catch (IOException e) {
      if (e.getMessage().equals("Unexpected EOF")) {
      	// ignore
      } else {
      	throw e;
      }
    }
    return result;
  }
}
