package com.laft.remittances.config;

import com.fasterxml.jackson.databind.Module;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module protobufModule() {
        return new ProtobufModule();
    }
}
