package dev.heartflame.fleet.model.s2c;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class S2CStressObject {

    private int count; // Count of bots
    private String ip; // IP to the server
    private int port; // Port to connect to
    private int interval; // Join interval
    private String nameFormat; // Format of the name - %node_id% = ID of the Node / %bot% - ID of the Bot (e.g. 50)

}
