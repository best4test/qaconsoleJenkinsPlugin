package com.qaconsole.plugins;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QACReporter extends Notifier {

    private String endPoint;
    private String apiKey;
    private String projectName;
    private String environment;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return  BuildStepMonitor.BUILD;
    }

    @DataBoundConstructor
    public QACReporter(String endPoint, String apiKey,String projectName,String environment) {
        this.endPoint = endPoint;
        this.apiKey = apiKey;
        this.projectName = projectName;
        this.environment = environment;
    }

    @Override
    public QACDescriptor getDescriptor() {
        return (QACDescriptor) super.getDescriptor();
    }


    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return null;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {


//        if(listener != null ) listener.getLogger().append("[qac] endPoint  " + endPoint +  "\n");
        String content = readFile(build.getRootDir() + "/junitResult.xml", StandardCharsets.UTF_8);

        String url=endPoint;
        URL object=new URL(url);

        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        JSONObject payload = new JSONObject();

        payload.put("projectName",projectName);
        payload.put("environment",environment);
        payload.put("ApiKey",apiKey);
        payload.put("result", content);

        OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
        wr.write(payload.toString());
        wr.flush();

        if(listener != null ) listener.getLogger().append("[qac] payload  " + payload +  "\n");

        //display what returns the POST request

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            if(listener != null ) listener.getLogger().append("[qac] response  " + sb.toString() +  "\n");
        } else {
            if(listener != null ) listener.getLogger().append("[qac] response error " + con.getResponseMessage() +  "\n");
        }

        return true;
    }

    private String readFile(String path, Charset utf8) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, utf8);
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }


}
