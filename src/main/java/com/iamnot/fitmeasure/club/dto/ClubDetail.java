package com.iamnot.fitmeasure.club.dto;

import java.util.List;

public record ClubDetail(String name, String address, String intro,
                         String type, List<ProgramInfo> programs) {}