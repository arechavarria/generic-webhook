package io.jenkins.plugins.genericwebhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.model.QueueTaskFuture;
import hudson.model.Run;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.Extension;
import jenkins.model.UnprotectedRootAction;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes a generic webhook endpoint to trigger pipelines.
 */
@Extension
public class GenericWebhookEndpoint implements UnprotectedRootAction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getUrlName() {
        return "generic-webhook";
    }

    @Override
    public String getDisplayName() {
        return "Generic Webhook";
    }

    @Override
    public String getIconFileName() {
        return null; // hidden from side-panel
    }

    /**
     * Handles POST requests to /generic-webhook/{pipelineName}.
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only POST supported");
            return;
        }

        String rest = req.getRestOfPath();
        if (rest == null || rest.isEmpty() || rest.equals("/")) {
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pipeline name required");
            return;
        }
        if (rest.startsWith("/")) {
            rest = rest.substring(1);
        }
        String pipelineName = rest;

        String body = req.getReader().lines()
                .reduce("", (a, b) -> a + b);

        try {
            MAPPER.readTree(body);
        } catch (IOException ex) {
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body");
            return;
        }

        WorkflowJob job = Jenkins.get().getItemByFullName(pipelineName, WorkflowJob.class);
        if (job == null) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Pipeline not found: " + pipelineName);
            return;
        }
        if (!job.isBuildable()) {
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Pipeline not buildable: " + pipelineName);
            return;
        }

        List<Action> actions = new ArrayList<>();
        actions.add(new ParametersAction(new StringParameterValue("webhookPayload", body)));
        actions.add(new CauseAction(new Cause.RemoteCause(req.getRemoteAddr(), "Triggered via generic webhook")));

        QueueTaskFuture<? extends Run<?, ?>> future = job.scheduleBuild2(0, actions.toArray(new Action[0]));
        if (future == null) {
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to schedule build");
            return;
        }

        rsp.setStatus(HttpServletResponse.SC_ACCEPTED);
        rsp.getWriter().println("Triggered pipeline " + pipelineName);
    }
}
