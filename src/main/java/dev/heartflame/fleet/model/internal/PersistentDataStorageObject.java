package dev.heartflame.fleet.model.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersistentDataStorageObject {

    private int interval;
    private int botCount;

}
