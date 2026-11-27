package com.gowayki.nest.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/** Ids explícitos a depurar. Si se omite, el backend detecta los obsoletos. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepurarGruposRequestDto {
    private List<UUID> conversacionIds;
}
