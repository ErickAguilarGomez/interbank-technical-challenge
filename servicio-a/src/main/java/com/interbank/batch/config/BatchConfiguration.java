package com.interbank.batch.config;

import com.interbank.batch.processor.TransactionItemProcessor;
import com.interbank.batch.reader.XmlTransactionReader;
import com.interbank.batch.writer.TransactionBatchWriter;
import com.interbank.common.dto.TransactionDto;
import com.interbank.common.entity.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

@Configuration
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${app.batch.chunk-size:50}")
    private int chunkSize;

    @Value("${app.batch.data-path:classpath:data/*.xml}")
    private String dataPath;

    @Bean
    @StepScope
    public MultiResourceItemReader<TransactionDto> multiResourceItemReader() throws IOException {
        MultiResourceItemReader<TransactionDto> reader = new MultiResourceItemReader<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(dataPath);

        Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
        log.info("Found {} XML files in path: {}", resources.length, dataPath);

        reader.setResources(resources);
        reader.setDelegate(new XmlTransactionReader());
        return reader;
    }

    @Bean
    public Step xmlProcessingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MultiResourceItemReader<TransactionDto> reader,
            TransactionItemProcessor processor,
            TransactionBatchWriter writer) {

        return new StepBuilder("xmlProcessingStep", jobRepository)
                .<TransactionDto, TransactionRecord>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job xmlTransactionJob(JobRepository jobRepository, Step xmlProcessingStep) {
        return new JobBuilder("xmlTransactionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(xmlProcessingStep)
                .build();
    }
}
