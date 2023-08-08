package com.prueba.tecnica.parqueaderos.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.prueba.tecnica.parqueaderos.general.constants.Constants;
import org.springframework.stereotype.Component;

@Component
public class AWSClient {
    public static final String REGION = Regions.US_EAST_1.getName();
    public static  String ENTRY_PARKING_URL;
    public static String EXIT_PARKING_URL;
    private final AwsProperties awsProperties;
    private AWSStaticCredentialsProvider awsStaticCredentialsProvider;

    public AWSClient(ApplicationProperties applicationProperties, AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
        ENTRY_PARKING_URL = String.format("%s%s", "", Constants.ENTRYPARKING_QUEUE);
        EXIT_PARKING_URL = String.format("%s%s", "", Constants.EXITPARKING_QUEUE);

    }

    public AWSStaticCredentialsProvider getAWSCredentials() {
        if(awsStaticCredentialsProvider == null) {
            BasicAWSCredentials awsCreeds = new BasicAWSCredentials(awsProperties.getAccessKey(), awsProperties.getSecretKey());
            awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(awsCreeds);
        }
        return awsStaticCredentialsProvider;
    }
}

