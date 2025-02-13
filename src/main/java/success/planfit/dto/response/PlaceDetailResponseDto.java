package success.planfit.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import success.planfit.domain.CachePlaceDetail;
import success.planfit.domain.course.SpaceType;
import success.planfit.dto.PlaceDetailMappingDto;
import success.planfit.photo.PhotoProvider;

@ToString
@Setter
@Getter
@Builder
public class PlaceDetailResponseDto {

    private String googlePlacesIdentifier;
    private String spaceName;

    // 형식화된 위치 이름
    private String location;

    // 타입 저장하는 방식
    private SpaceType spaceTag;
    private String link;
    private Double lat;
    private Double lng;
    private String googleMapLinks;
    private String  spacePhoto;



    // 캐시 -> reponseDto
    public static PlaceDetailResponseDto createFromCache(CachePlaceDetail cachePlacedetail){
        return PlaceDetailResponseDto.builder()
                .googlePlacesIdentifier(cachePlacedetail.getGooglePlacesIdentifier())
                .spaceName(cachePlacedetail.getSpaceInformation().getSpaceName())
                .location(cachePlacedetail.getSpaceInformation().getLocation())
                .spaceTag(cachePlacedetail.getSpaceInformation().getSpaceTag())
                .link(cachePlacedetail.getSpaceInformation().getLink())
                .lat(cachePlacedetail.getSpaceInformation().getLatitude())
                .lng(cachePlacedetail.getSpaceInformation().getLongitude())
                .spacePhoto(PhotoProvider.encode(cachePlacedetail.getSpaceInformation().getSpacePhoto()))
                .build();
    }

    // mappingDto -> responseDto
    public static PlaceDetailResponseDto createFromMapper(PlaceDetailMappingDto mapper){
        return PlaceDetailResponseDto.builder()
                .googlePlacesIdentifier(mapper.getGooglePlacesIdentifier())
                .spaceName(mapper.getSpaceName())
                .location(mapper.getLocation())
                .spaceTag(mapper.getSpaceTag())
                .link(mapper.getLink())
                .lat(mapper.getLat())
                .lng(mapper.getLng())
                .spacePhoto(mapper.getSpacePhoto())
                .build();
    }

}
