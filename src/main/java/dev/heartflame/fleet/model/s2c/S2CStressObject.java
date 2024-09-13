package dev.heartflame.fleet.model.s2c;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S2CStressObject {

    private int id; // ID of the Node
    private String type; // internal use
    private boolean overrideCount;
    private int botCount;
    private String ip; // IP to the server
    private int port; // Port to connect to
    private int interval; // Join interval
    // %id% - ID of the Bot (e.g. 50) - THIS DIFFERS WITH THE NODE ID!
    private String nameFormat; // e.g. format was "Bot_%id%" - node 1 would do Bot_1 to Bot_50. Node 2 would do Bot_51 to Bot_100 etc

}