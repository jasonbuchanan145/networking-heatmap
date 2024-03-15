package edu.usd.jbuchan.ipheatmap.tshark;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**Tshark exports in the form of
'{"timestamp":"1710532065797","layers":{"ip_src":["192.168.0.20"],"ip_dst":["162.254.192.75"],"frame_len":["108"]}}
which is not a great structure for what I want to do so make a custom deserializer to deserialize that to a flat object
*/
 public class SharkDeserializer extends StdDeserializer<Shark> {

    public SharkDeserializer() {
        this(null);
    }

    public SharkDeserializer(Class<?> c) {
        super(c);
    }

    @Override
    public Shark deserialize(JsonParser jp, DeserializationContext ctxt)throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);
        //the layers child object of the string above
        JsonNode layersNode = node.get("layers");
        Shark shark = new Shark();
        shark.setTime(node.get("timestamp").asLong());
        shark.setSrcIp(layersNode.get("ip_src").get(0).asText());
        shark.setDestIp(layersNode.get("ip_dst").get(0).asText());
        shark.setPacketSize(Integer.parseInt(layersNode.get("frame_len").get(0).asText()));
        return shark;
    }
}

