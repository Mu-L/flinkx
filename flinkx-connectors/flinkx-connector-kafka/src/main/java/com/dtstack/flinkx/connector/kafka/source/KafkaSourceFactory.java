/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.connector.kafka.source;

import com.dtstack.flinkx.converter.RawTypeConverter;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.table.data.RowData;

import com.dtstack.flinkx.conf.SyncConf;
import com.dtstack.flinkx.connector.kafka.adapter.StartupModeAdapter;
import com.dtstack.flinkx.connector.kafka.conf.KafkaConf;
import com.dtstack.flinkx.connector.kafka.enums.StartupMode;
import com.dtstack.flinkx.connector.kafka.serialization.RowDeserializationSchema;
import com.dtstack.flinkx.connector.kafka.util.KafkaUtil;
import com.dtstack.flinkx.source.SourceFactory;
import com.dtstack.flinkx.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Properties;

/**
 * Date: 2019/11/21
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class KafkaSourceFactory extends SourceFactory {

    protected KafkaConf kafkaConf;

    public KafkaSourceFactory(SyncConf config, StreamExecutionEnvironment env) {
        super(config, env);
        Gson gson = new GsonBuilder().registerTypeAdapter(StartupMode.class, new StartupModeAdapter()).create();
        GsonUtil.setTypeAdapter(gson);
        kafkaConf = gson.fromJson(gson.toJson(config.getReader().getParameter()), KafkaConf.class);
    }

    @Override
    public DataStream<RowData> createSource() {
        Properties props = new Properties();
        props.put("group.id", kafkaConf.getGroupId());
        props.putAll(kafkaConf.getConsumerSettings());
        FlinkKafkaConsumer<RowData> consumer = new FlinkKafkaConsumer<>(
                kafkaConf.getTopic(),
                new RowDeserializationSchema(kafkaConf.getCodec(), typeInformation),
                props);
        switch (kafkaConf.getMode()){
            case EARLIEST:
                consumer.setStartFromEarliest();
                break;
            case LATEST:
                consumer.setStartFromLatest();
                break;
            case TIMESTAMP:
                consumer.setStartFromTimestamp(kafkaConf.getTimestamp());
                break;
            case SPECIFIC_OFFSETS:
                consumer.setStartFromSpecificOffsets(KafkaUtil.parseSpecificOffsetsString(kafkaConf.getTopic(), kafkaConf.getOffset()));
            default:
                consumer.setStartFromGroupOffsets();
                break;
        }
        consumer.setCommitOffsetsOnCheckpoints(kafkaConf.getGroupId() != null);
        return createInput(consumer, syncConf.getReader().getName());
    }

    @Override
    public RawTypeConverter getRawTypeConverter() {
        throw new UnsupportedOperationException("Kafka" + NO_SUPPORT_MSG);
    }
}