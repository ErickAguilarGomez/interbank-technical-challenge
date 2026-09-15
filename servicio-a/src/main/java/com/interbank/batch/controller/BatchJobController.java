package com.interbank.batch.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final JobExplorer jobExplorer;

    public BatchJobController(JobLauncher jobLauncher, Job job, JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);

            Map<String, Object> response = new HashMap<>();
            response.put("jobExecutionId", execution.getId());
            response.put("status", execution.getStatus().toString());
            response.put("startTime", execution.getStartTime());
            response.put("message", "Job de procesamiento batch iniciado con éxito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/status/{executionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = buildExecutionResponse(execution);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLatestJobStatus() {
        var executions = jobExplorer.findJobInstancesByJobName("xmlTransactionJob", 0, 1);
        if (executions.isEmpty()) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "No hay ejecuciones registradas");
            return ResponseEntity.ok(resp);
        }

        JobExecution latest = jobExplorer.getJobExecutions(executions.get(0)).stream().findFirst().orElse(null);
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(buildExecutionResponse(latest));
    }

    private Map<String, Object> buildExecutionResponse(JobExecution execution) {
        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", execution.getId());
        response.put("status", execution.getStatus().toString());
        response.put("exitStatus", execution.getExitStatus().getExitCode());
        response.put("startTime", execution.getStartTime());
        response.put("endTime", execution.getEndTime());

        var stepExecutions = execution.getStepExecutions();
        if (!stepExecutions.isEmpty()) {
            var step = stepExecutions.iterator().next();
            response.put("readCount", step.getReadCount());
            response.put("writeCount", step.getWriteCount());
            response.put("skipCount", step.getSkipCount());
            response.put("commitCount", step.getCommitCount());
            response.put("rollbackCount", step.getRollbackCount());
        }
        return response;
    }
}

