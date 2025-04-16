package success.planfit.post.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import success.planfit.entity.like.PostLike;
import success.planfit.entity.post.Post;
import success.planfit.entity.user.User;
import success.planfit.repository.PostLikeRepository;
import success.planfit.repository.PostRepository;
import success.planfit.repository.UserRepository;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private static final Supplier<EntityNotFoundException> USER_NOT_FOUND_EXCEPTION = () -> new jakarta.persistence.EntityNotFoundException("유저 조회에 실패했습니다.");
    private static final Supplier<EntityNotFoundException> POST_NOT_FOUND_EXCEPTION = () -> new EntityNotFoundException("해당 ID를 지닌 포스트를 찾을 수 없습니다.");

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void likePost(long postId, long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(USER_NOT_FOUND_EXCEPTION);

        Post post = postRepository.findById(postId)
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);

        postLikeRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(like -> {
                    throw new IllegalArgumentException("이미 좋아요한 포스트입니다.");
                });

        postLikeRepository.save(PostLike.builder()
                .user(user)
                .post(post)
                .build());
        post.increaseLikeCount();
    }

    @Transactional(readOnly = true)
    public List<Long> getLikedPosts(long userId) {
        return postLikeRepository.findByUserId(userId).
                stream()
                .map(like -> like.getPost().getId())
                .toList();
    }

    public void unlikePost(long postId, long userId) {
        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(POST_NOT_FOUND_EXCEPTION);

        postLikeRepository.delete(postLike);
        postLike.getPost().decreaseLikeCount();
    }

}

