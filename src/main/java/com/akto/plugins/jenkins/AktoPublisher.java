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

public class AktoPublisher extends Recorder implements SimpleBuildStep {

    private final String aktoDashboardUrl;
    private final String aktoApiKey;
    private final String aktoTestId;

    @DataBoundConstructor
    public AktoPublisher(String aktoDashboardUrl, String aktoApiKey, String aktoTestId) {
        this.aktoDashboardUrl = aktoDashboardUrl;
        this.aktoApiKey = aktoApiKey;
        this.aktoTestId = aktoTestId;
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

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
     
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(aktoTestId, JSON);

        Request request = 
            new Request.Builder()
            .url(aktoDashboardUrl + "api/startTest")
            .header("Content-Type", "application/json")
            .header("X-API-KEY", aktoApiKey)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            listener.getLogger().println("Triggered API test in Akto successfully!");
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
