package success.planfit.util;

import org.springframework.stereotype.Component;
import success.planfit.course.dto.CourseRequestDto;
import success.planfit.course.dto.CourseResponseDto;
import success.planfit.course.dto.SpaceRequestDto;
import success.planfit.course.dto.SpaceResponseDto;
import success.planfit.entity.course.Course;
import success.planfit.entity.schedule.Schedule;
import success.planfit.entity.space.Space;
import success.planfit.entity.space.SpaceDetail;
import success.planfit.entity.space.SpacePhoto;
import success.planfit.global.photo.PhotoProvider;
import success.planfit.schedule.dto.request.ScheduleRequestDto;
import success.planfit.schedule.dto.response.ScheduleResponseDto;
import success.planfit.schedule.dto.response.ScheduleTitleInfoResponseDto;

import java.util.List;

@Component
public class EqualityChecker {

    public boolean check(Schedule schedule, ScheduleRequestDto scheduleDto) {
        return schedule.getTitle().equals(scheduleDto.getTitle()) &&
                schedule.getDate().equals(scheduleDto.getDate()) &&
                schedule.getContent().equals(scheduleDto.getContent()) &&
                schedule.getStartTime().equals(scheduleDto.getStartTime()) &&
                check(schedule.getCourse(), scheduleDto.getCourse());
    }

    public boolean check(Schedule schedule, ScheduleResponseDto scheduleDto) {
        return schedule.getId().equals(scheduleDto.getScheduleId()) &&
                schedule.getTitle().equals(scheduleDto.getTitle()) &&
                schedule.getDate().equals(scheduleDto.getDate()) &&
                schedule.getContent().equals(scheduleDto.getContent()) &&
                schedule.getStartTime().equals(scheduleDto.getStartTime()) &&
                schedule.getCurrentSequence().equals(scheduleDto.getCurrentSequence()) &&
                check(schedule.getCourse(), scheduleDto.getCourse());
    }

    public boolean check(ScheduleRequestDto requestDto, ScheduleTitleInfoResponseDto responseDto) {
        CourseRequestDto courseDto = requestDto.getCourse();

        return requestDto.getTitle().equals(responseDto.getTitle()) &&
                requestDto.getDate().equals(responseDto.getDate()) &&
                requestDto.getStartTime().equals(responseDto.getStartTime()) &&
                courseDto.getLocation().equals(responseDto.getLocation());
    }

    public boolean check(Course course, CourseRequestDto courseDto) {
        return course.getLocation().equals(courseDto.getLocation()) &&
                checkSpacesWithSpaceRequestDtos(course.getSpaces(), courseDto.getSpaces());
    }

    public boolean check(Course course, CourseResponseDto courseDto) {
        return course.getId().equals(courseDto.getCourseId()) &&
                course.getLocation().equals(courseDto.getLocation()) &&
                checkSpacesWithSpaceResponseDtos(course.getSpaces(), courseDto.getSpaces());
    }

    public boolean check(Space space, SpaceRequestDto spaceDto) {
        SpaceDetail spaceDetail = space.getSpaceDetail();

        return spaceDetail.getGooglePlacesIdentifier().equals(spaceDto.getGooglePlacesIdentifier());
    }

    public boolean check(Space space, SpaceResponseDto spaceDto) {
        SpaceDetail spaceDetail = space.getSpaceDetail();
        List<String> spacePhotos = getEncodedSpacePhotos(spaceDetail);

        return space.getSequence().equals(spaceDto.getSequence()) &&
                spaceDetail.getGooglePlacesIdentifier().equals(spaceDto.getGooglePlacesIdentifier()) &&
                spaceDetail.getSpaceName().equals(spaceDto.getName()) &&
                spaceDetail.getLocation().equals(spaceDto.getLocation()) &&
                spaceDetail.getSpaceType().equals(spaceDto.getSpaceType()) &&
                spaceDetail.getLink().equals(spaceDto.getLink()) &&
                spaceDetail.getLatitude().equals(spaceDto.getLatitude()) &&
                spaceDetail.getLongitude().equals(spaceDto.getLongitude()) &&
                spacePhotos.equals(spaceDto.getSpacePhotos());
    }

    private boolean checkSpacesWithSpaceRequestDtos(List<Space> spaces, List<SpaceRequestDto> spaceDtos) {
        if (spaces.size() != spaceDtos.size()) {
            return false;
        }

        for (int i = 0; i < spaces.size(); i++) {
            if (!check(spaces.get(i), spaceDtos.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkSpacesWithSpaceResponseDtos(List<Space> spaces, List<SpaceResponseDto> spaceDtos) {
        if (spaces.size() != spaceDtos.size()) {
            return false;
        }

        for (int i = 0; i < spaces.size(); i++) {
            if (!check(spaces.get(i), spaceDtos.get(i))) {
                return false;
            }
        }

        return true;
    }

    private List<String> getEncodedSpacePhotos(SpaceDetail spaceDetail) {
        return spaceDetail.getSpacePhotos().stream()
                .map(SpacePhoto::getValue)
                .map(PhotoProvider::encode)
                .toList();
    }

}
