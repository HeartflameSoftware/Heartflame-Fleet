package dev.heartflame.fleet.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@AllArgsConstructor
@Getter
@Setter
public class RepeatingAction {

    private String id;
    private ScheduledExecutorService service;
    private String interval;
    private ActionType actionType;
    private String action;
    private ScheduledFuture<?> task;

}
