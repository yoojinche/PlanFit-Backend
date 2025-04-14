package success.planfit.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import success.planfit.course.dto.SpaceRequestDto;
import success.planfit.entity.comment.Comment;
import success.planfit.entity.course.Course;
import success.planfit.entity.post.Post;
import success.planfit.entity.post.PostPhoto;
import success.planfit.entity.post.PostType;
import success.planfit.entity.post.PostTypeValue;
import success.planfit.entity.space.Space;
import success.planfit.entity.space.SpaceDetail;
import success.planfit.entity.user.User;
import success.planfit.global.photo.PhotoProvider;
import success.planfit.post.dto.request.PostRequestDto;
import success.planfit.post.dto.response.PostInfoDto;
import success.planfit.course.dto.CourseResponseDto;
import success.planfit.global.exception.EntityNotFoundException;
import success.planfit.global.exception.IllegalRequestException;
import success.planfit.repository.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PostService {
    private static final Supplier<EntityNotFoundException> POST_NOT_FOUND_EXCEPTION = () -> new EntityNotFoundException("해당 ID를 지닌 포스트를 찾을 수 없습니다.");

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SpaceDetailRepository spaceDetailRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostLikeRepository postLikeRepository;

    // 사용자가 코스 생성해서 포스팅
    public void registerPost(Long userId, PostRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 조회 실패"));

        Post post = createPost(requestDto);
        Course course = createCourse(requestDto);
        List<Space> spaces = createSpaces(requestDto.getSpaces());
        List<PostPhoto> postPhotos = createPostPhoto(requestDto.getPostPhotos());
        List<PostType> postTypes = createPostType(requestDto.getPostTypes());

        connectEntities(user, post, course, spaces, postPhotos, postTypes);
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public CourseResponseDto findCourseInPublicPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);
        validatePublic(post);

        return CourseResponseDto.from(post.getCourse());
    }

    private void validatePublic(Post post) {
        if (!post.getIsPublic()) {
            throw new IllegalRequestException("비공개 포스트의 정보는 조회할 수 없습니다.");
        }
    }

    // 포스트 단건 조회
    public PostInfoDto findPost(Long postId) {
        Post post = postRepository.findById(postId).stream()
                .filter(postForFilter -> postForFilter.getId().equals(postId))
                .findAny()
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);

        return PostInfoDto.from(post);
    }

    // 포스트 3건 조회 - 최신순
    public List<PostInfoDto> findRecentPosts(int n) {
        Optional<List<Post>> posts = postRepository.findTop3ByOrderByCreatedAtDesc(n);

        List<PostInfoDto> postInfoDtos = posts.get().stream()
                .map(PostInfoDto::from)
                .toList();

        return postInfoDtos;
    }

    // 모든 포스트 최신순 조회
    public List<PostInfoDto> findAllOrderByCreatedAtDesc(){
        Optional<List<Post>> posts = postRepository.findAllOrderByCreatedAtDesc();
        List<PostInfoDto> postInfoDtos = posts.get().stream()
                .map(PostInfoDto::from)
                .toList();
        return postInfoDtos;
    }

    // 포스트 수정
    public void updatePost(Long userId, Long postId, PostRequestDto requestDto) {
        // 나중에 get하는 거 보고 join fetch 하겠슴, course도 같이 ㄱㄱ,,
        Post post = postRepository.findByIdWithUserAndCourse(postId).stream()
                .filter(postForFilter -> postForFilter.getUser().getId().equals(userId))
                .findAny()
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);

        List<Space> spaces = createSpaces(requestDto.getSpaces());
        Course course = post.getCourse();
        List<PostPhoto> postPhotos = createPostPhoto(requestDto.getPostPhotos());
        List<PostType> postTypes = createPostType(requestDto.getPostTypes());

        course.update(requestDto.getLocation());
        post.update(requestDto);
        replaceSpaces(course, spaces);
        replacePostPhotoAndPost(post, postPhotos, postTypes);
    }

    // 포스트 삭제
    public void deletePost(Long userId, Long postId) {
        // 유저 조회
        User user = userRepository.findByIdWithPost(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저 조회 실패"));
        Post post = user.getPosts().stream()
                .filter(p -> p.getId().equals(postId))
                .findAny()
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);
        List<Long> commentIds = post.getComments().stream()
                .map(Comment::getId)
                .toList();

        commentLikeRepository.deleteAllByCommentIdIn(commentIds);
        postLikeRepository.findByUserIdAndPostId(userId, postId).stream()
                .forEach(user::removePostLike);
        user.removePost(post);
    }

    private static Post createPost(PostRequestDto requestDto) {
        return Post.builder()
                .content(requestDto.getContent())
                .title(requestDto.getTitle())
                .isPublic(requestDto.getIsPublic())
                .build();
    }

    private Course createCourse(PostRequestDto requestDto) {
        return Course.builder()
                .location(requestDto.getLocation())
                .build();
    }

    private List<Space> createSpaces(List<SpaceRequestDto> requestDto){
        ArrayList<Space> spaces = new ArrayList<>();

        int sequence = 0;
        for (SpaceRequestDto spaceRequestDto : requestDto) {
            SpaceDetail spaceDetail = spaceDetailRepository.findByGooglePlacesIdentifier(spaceRequestDto.getGooglePlacesIdentifier())
                    .orElseThrow(POST_NOT_FOUND_EXCEPTION);
            spaces.add(Space.createSpace(spaceDetail, sequence));

            sequence++;
        }
        return Collections.unmodifiableList(spaces);
    }

    private List<PostPhoto> createPostPhoto(List<String> postPhotos){
        return postPhotos.stream()
                .map(PhotoProvider::decode)
                .map(postPhoto -> {
                    return PostPhoto.builder()
                            .photo(postPhoto)
                            .build();
                })
                .toList();
    }

    private List<PostType> createPostType(List<String> postTypes){
        return postTypes.stream()
                .map(postType -> {
                    return PostType.builder()
                            .value(PostTypeValue.valueOf(postType))
                            .build();
                })
                .toList();
    }

    private void connectEntities(User user, Post post, Course course, List<Space> spaces
                                ,List<PostPhoto> postPhotos, List<PostType> postTypes) {
        course.addSpaces(spaces);
        post.addPostTypes(postTypes);
        post.addPostPhotos(postPhotos);
        post.setCourse(course);
        user.addPost(post);
    }

    private void replaceSpaces(Course course, List<Space> spaces) {
        course.removeEverySpace();
        course.addSpaces(spaces);
    }

    private void replacePostPhotoAndPost(Post post, List<PostPhoto> postPhotos, List<PostType> postTypes) {
        post.removeEveryPostPhotos();
        post.removeEveryPostTypes();
        post.addPostPhotos(postPhotos);
        post.addPostTypes(postTypes);
    }
}
