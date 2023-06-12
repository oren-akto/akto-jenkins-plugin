package com.akto.plugins.jenkins;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.jenkinsci.Symbol;

import org.json.JSONObject;

public class AktoPublisher extends Recorder implements SimpleBuildStep {

    private final String aktoDashboardUrl;
    private final String aktoApiKey;
    private final String aktoTestId;
    private final String aktoStartTestDelay;

    @DataBoundConstructor
    public AktoPublisher(String aktoDashboardUrl, String aktoApiKey, String aktoTestId, String aktoStartTestDelay) {
        this.aktoDashboardUrl = aktoDashboardUrl;
        this.aktoApiKey = aktoApiKey;
        this.aktoTestId = aktoTestId;
        this.aktoStartTestDelay = aktoStartTestDelay;
    }

    public String getAktoDashboardUrl() {
        return aktoDashboardUrl;
    }

    public String getAktoApiKey() {
        return aktoApiKey;
    }

    public String getAktoTestId() {
        return aktoTestId;
    }

    public String getAktostartTestDelay() {
        return aktoStartTestDelay;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        long startTimestamp = 0;

        if (!aktoStartTestDelay.equals("")) {
            try {
                int delay = Integer.parseInt(aktoStartTestDelay);
                startTimestamp = System.currentTimeMillis() / 1000 + delay;
            } catch (NumberFormatException e) {
                listener.getLogger().println("aktoStartTestDelay should be an integer" + e);
            }
        }

        String aktoStartTestEndpoint = "";

        if (aktoDashboardUrl.endsWith("/")) {
            aktoStartTestEndpoint = aktoDashboardUrl + "api/startTest";
        } else {
            aktoStartTestEndpoint = aktoDashboardUrl + "/api/startTest";
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject aktoTestJson = new JSONObject();
        aktoTestJson.put("testingRunHexId", aktoTestId);
        aktoTestJson.put("startTimestamp", startTimestamp);  
        
        JSONObject metadata = new JSONObject();
        metadata.put("platform", "Jenkins");

        //todo: add metadata from built in Jenkins variables
        metadata.put("repository_url", env.getOrDefault("GIT_URL", ""));
        metadata.put("branch", env.getOrDefault("GIT_BRANCH", ""));
        metadata.put("commit_sha", env.getOrDefault("GIT_COMMIT", ""));

        aktoTestJson.put("metadata", metadata);        

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(aktoTestJson.toString(), JSON);

        Request request = 
            new Request.Builder()
            .url(aktoStartTestEndpoint)
            .header("Content-Type", "application/json")
            .header("X-API-KEY", aktoApiKey)
            .post(body)
            .build();

        String resultsUrl = aktoDashboardUrl + "dashboard/testing/" + aktoTestId + "/results";

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            listener.getLogger().println("Triggered API test in Akto successfully!");
            listener.getLogger().println("You can view the results at " + resultsUrl);
        } catch (Exception e) {
            listener.getLogger().println("Could not trigger Akto CI/CD test!" + e);
        }
    }

    @Symbol("akto")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.AktoPublisher_DescriptorImpl_DisplayName();
        }

    }

}
