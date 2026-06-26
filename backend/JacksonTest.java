import com.fasterxml.jackson.databind.ObjectMapper;
import com.laft.commons.grpc.client.ClientResponse;

public class JacksonTest {
    public static void main(String[] args) throws Exception {
        ClientResponse response = ClientResponse.newBuilder()
            .setClientId(123)
            .setName("John Doe")
            .setAge(30)
            .build();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(response));
    }
}
