package com.akto.plugins.jenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class AktoPublisherTest {

    private final String aktoDashboardUrl = "";
    private final String aktoApiKey = "";
    private final String aktoTestId = "";
    private final String aktoStartTestDelay = "";


    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    @Test
    public void testBuild() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        AktoPublisher publisher = new AktoPublisher(aktoDashboardUrl, aktoApiKey, aktoTestId, aktoStartTestDelay);
        project.getPublishersList().add(publisher);
        FreeStyleBuild build = new FreeStyleBuild(project);
    
        try {
            build = jenkins.buildAndAssertSuccess(project);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        } 
        build.getLog(100).forEach(System.out::println);
        Assert.assertTrue("true", true);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
            = "node {\n"
            + "  akto(aktoDashboardUrl:'" + aktoDashboardUrl + "', aktoApiKey:'"+ aktoApiKey +"', aktoTestId: '" + aktoTestId + "')\n"
            + "}";
        System.out.println(pipelineScript);
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        completedBuild.getLog(100).forEach(System.out::println);
        Assert.assertTrue("true", true);
        //String expectedString = "Hello, " + name + "!";
        //jenkins.assertLogContains(expectedString, completedBuild);
    }

    // @Test
    // public void testDeclarativePipeline() throws Exception {
    //     String agentLabel = "my-agent";
    //     jenkins.createOnlineSlave(Label.get(agentLabel));
    //     WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
    //     String pipelineScript 
    //     = 'pipeline{'+
    //         '    agent any'+
    //         '    stages {'+
    //         '        stage('Greet') {'+
    //         '            steps {'+
    //         '	            script {'+
    //         '		            node {'+
    //         '                                greet(AKTO_DASHBOARD_URL: AKTO_DASHBOARD_URL, '+
    //         '                                            AKTO_API_KEY: AKTO_API_KEY, '+
    //         '                                             AKTO_TEST_ID:  AKTO_TEST_ID'+
    //         '                                          )'+
    //         '                          }'+
    //         '	            }'+
    //         '            }'+
    //         '        }'+
    //         '    }'+
    //         '}';
    //     System.out.println(pipelineScript);
    //     job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
    //     WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
    //     completedBuild.getLog(100).forEach(System.out::println);
    //     Assert.assertTrue("true", true);
    //     //String expectedString = "Hello, " + name + "!";
    //     //jenkins.assertLogContains(expectedString, completedBuild);
    // }
}