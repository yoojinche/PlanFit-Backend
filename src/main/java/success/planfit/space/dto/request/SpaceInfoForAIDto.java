package success.planfit.space.dto.request;

import lombok.Builder;
import lombok.Getter;
import success.planfit.course.dto.SpaceResponseDto;
import success.planfit.entity.user.User;
import success.planfit.post.dto.response.PostInfoDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class SpaceInfoForAIDto {
    private Double latitude;
    private Double longitude;
    private String spaceType;
    // 사용자 정보 -> TODO: SpaceLike와 PostLike 병합 후 수정 필요
    private List<SpaceResponseDto> spaceList;
    private List<PostInfoDto> postList;

    // TODO: 사용자의 리뷰 추가 필요

    // DTO로 만들어주는 메소드가 필요함
    public static SpaceInfoForAIDto of(User user, SpaceDetailRequestDto requestDto){
        return SpaceInfoForAIDto.builder()
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .spaceType(requestDto.getSpaceType())
//                .spaceList(user.getSpaceLikes().stream().map(SpaceResponseDto::from).toList())
//                .postList(user.getPostLikes().stream().map(PostInfoDto::from).toList())
                .build();
    }
}
