package com.songsong.rent.vo;

import com.songsong.rent.entity.House;
import com.songsong.rent.entity.HouseImage;
import com.songsong.rent.entity.LabelInfo;
import lombok.Data;

import java.util.List;

@Data
public class HouseDetailVO {

    private House house;

    private List<HouseImage> images;

    private List<LabelInfo> labels;
}
