package com.example.yang;

import com.google.gson.stream.JsonReader;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;


import java.io.*;

import java.util.*;

/**
 * This Class is used to parse Yang files and validate json using opendaylight API
 */

@SpringBootApplication
@RestController
@RequestMapping(value = "/config")

public class ConfigValidator {

    public static EffectiveModelContext schemaContext;

    public ConfigValidator() {

    }


    /**
     * This Function is to send response to client after validation.
     * @param jsonData
     * @return
     */

	@PostMapping(value = "/dns/setconfig")
    private String dnsSetConfig(@RequestBody String jsonData) {
        System.out.println("Calling HTTP POST /dns/setConfig");
        return parseJSON(jsonData);
    }


    /**
     * This Function will parse the jsonData getting from HTTP request and validate data based on the semantics defined
     * in all the .yang files
     * @param jsonData
     * @return
     */
    private String parseJSON(String jsonData){

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        JSONCodecFactory codecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);

        try {
            JsonParserStream jsonParserStream = JsonParserStream.create(streamWriter, codecFactory)
                    .parse(new JsonReader(new StringReader(jsonData)));
            NormalizedNode<?, ?> transformedInput = result.getResult();
            System.out.println("transformed Input is : " + transformedInput.getValue());
            System.out.println("Finished = " + result.isFinished());
        }
        catch (Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
        return result.isFinished() ? "Ok" : "Not Valid";
    }

    /**
     * parse all the .yang files in YANG_FILE_DIR and define schemaContext
     * @throws ReactorException
     */
    public void initSchemaContext() throws ReactorException {


            List<StatementStreamSource> streamSourceList = new LinkedList<>();
            System.out.println("Initializing Schema ");

            String yangFilesDir = "YANG_FILE_DIR";
            try {
                File dir = new File(yangFilesDir);
                File[] files = dir.listFiles((d, name) -> name.endsWith(".yang"));
                for (File file : files) {

//                    System.out.println("file name is :" + file.getName());
                    YangTextSchemaSource yangTextSchemaSource = YangTextSchemaSource.forFile(new File(String.valueOf(file)));
                    StatementStreamSource yangSource = YangStatementStreamSource.create(yangTextSchemaSource);
                    streamSourceList.add(yangSource);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            CrossSourceStatementReactor.BuildAction reactor = RFC7950Reactors.defaultReactor().newBuild();
            reactor.addSources(streamSourceList);
            schemaContext = reactor.buildEffective();

    }


    public static void main(String[] args) throws ReactorException {

		SpringApplication.run(ConfigValidator.class, args);
		ConfigValidator configValidator = new ConfigValidator();
		configValidator.initSchemaContext();

    }
}
