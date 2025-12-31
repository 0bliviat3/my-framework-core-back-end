# Framework 모듈 개발 스킬

## 목적
Spring Boot 기반 Framework 프로젝트에서 새로운 모듈(도메인)을 일관되게 구현하기 위한 표준 개발 가이드입니다.
단위 테스트까지 포함하여 검증 가능한 코드를 작성합니다.

## 사용 시점
- 새로운 도메인 모듈을 추가할 때
- 기존 모듈을 확장할 때
- 일관된 코드 구조가 필요할 때

## 프로젝트 아키텍처

```
com.wan.framework.{domain}
├── domain/          # 엔티티 (JPA Entity)
├── dto/             # 데이터 전송 객체
├── repository/      # JPA Repository
├── service/         # 비즈니스 로직
├── web/             # Controller (REST API)
├── mapper/          # MapStruct Mapper (Entity ↔ DTO)
├── exception/       # 도메인별 예외
└── constant/        # 도메인별 상수
```

## 코딩 컨벤션

### 1. 패키지 구조
- 패키지명: `com.wan.framework.{도메인명}`
- 도메인명은 소문자 단수형 사용 (예: `user`, `board`, `menu`)

### 2. 네이밍 규칙

#### 엔티티 (Entity)
- 클래스명: 도메인명 (단수형, PascalCase)
- 예: `User`, `BoardMeta`, `Menu`
- 테이블명: `t_{도메인명}` (스네이크_케이스)
- 예: `t_user`, `t_board_meta`

#### DTO
- 클래스명: `{도메인명}DTO`
- 예: `UserDTO`, `BoardMetaDTO`

#### Repository
- 인터페이스명: `{도메인명}Repository`
- `JpaRepository<Entity, ID타입>` 상속
- 예: `UserRepository extends JpaRepository<User, String>`

#### Service
- 클래스명: `{도메인명}Service`
- 비즈니스 로직 담당
- 예: `UserService`, `BoardMetaService`

#### Controller
- 클래스명: `{도메인명}Controller`
- `@RestController` + `@RequestMapping` 사용
- 예: `UserController`, `BoardMetaController`

#### Mapper
- 인터페이스명: `{도메인명}Mapper`
- `@Mapper(componentModel = "spring")` 사용
- 예: `UserMapper`

#### Exception
- 클래스명: `{도메인명}Exception`
- `FrameworkException` 상속
- 예: `UserException`, `BoardException`

#### 예외 메시지 상수
- Enum 클래스명: `{도메인명}ExceptionMessage`
- `ExceptionConst` 인터페이스 구현
- 예: `UserExceptionMessage`, `BoardExceptionMessage`

### 3. 어노테이션 규칙

#### Entity
```java
@Entity
@Table(name = "t_도메인명")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
```

#### DTO
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

#### Service
```java
@Slf4j
@Service
@RequiredArgsConstructor
```

#### Controller
```java
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/도메인명s")  // 복수형
```

#### Repository
```java
public interface {도메인명}Repository extends JpaRepository<Entity, ID> {
    // 삭제된 데이터 제외 조회 메서드
    Optional<Entity> findByIdAndDataStateCodeNot(ID id, DataStateCode dataStateCode);
    Page<Entity> findAllByDataStateCodeNot(Pageable pageable, DataStateCode dataStateCode);
}
```

### 4. 공통 필드 규칙

모든 엔티티는 다음 공통 필드를 포함:

```java
@Column(name = "create_time", nullable = false)
private LocalDateTime createTime;

@Column(name = "modified_time")
private LocalDateTime modifiedTime;

@Column(name = "data_code")
@Enumerated(EnumType.STRING)
private DataStateCode dataCode;

@PrePersist
protected void onCreate() {
    this.createTime = LocalDateTime.now();
    this.dataCode = DataStateCode.I;  // Insert
}

@PreUpdate
protected void onUpdate() {
    this.modifiedTime = LocalDateTime.now();
}
```

### 5. 데이터 상태 관리

- `DataStateCode.I`: Insert (생성)
- `DataStateCode.U`: Update (수정)
- `DataStateCode.D`: Delete (삭제)

**삭제는 물리적 삭제가 아닌 논리적 삭제 사용**:
```java
public EntityDTO deleteEntity(Long id) {
    Entity entity = repository.findByIdAndDataStateCodeNot(id, DataStateCode.D)
        .orElseThrow(() -> new DomainException(NOT_FOUND));
    entity.setDataCode(DataStateCode.D);
    return mapper.toDto(repository.save(entity));
}
```

### 6. 트랜잭션 처리

- CUD(Create, Update, Delete) 작업에는 `@Transactional` 필수
- 조회(Read)는 `@Transactional(readOnly = true)` 권장

```java
@Transactional
public EntityDTO createEntity(EntityDTO dto) {
    // 생성 로직
}

@Transactional(readOnly = true)
public EntityDTO findById(Long id) {
    // 조회 로직
}
```

### 7. 예외 처리

#### 예외 메시지 상수 정의
```java
public enum UserExceptionMessage implements ExceptionConst {
    NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다"),
    INVALID_PASSWORD("USER_002", "비밀번호가 일치하지 않습니다"),
    USED_ID("USER_003", "이미 사용 중인 아이디입니다");

    private final String code;
    private final String message;

    UserExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

#### 예외 발생
```java
throw new UserException(UserExceptionMessage.NOT_FOUND);
```

### 8. 페이징 처리

Controller에서 페이징 파라미터 받기:
```java
@GetMapping
public Page<EntityDTO> getEntityList(
    @RequestParam(value = "page", defaultValue = "0") int pageNumber,
    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
    return service.findAll(pageRequest);
}
```

### 9. API 응답 규칙

#### 성공 응답
- 객체 반환: `ResponseEntity.ok(object)`
- 문자열 반환: `ResponseEntity.ok("성공 메시지")`
- 삭제 성공: `ResponseEntity.noContent().build()`

#### 에러 응답
- `FrameworkExceptionHandler`에서 자동 처리
- HTTP 400 Bad Request + ExceptionResponse 반환

### 10. 보안 민감 정보 처리

비밀번호 등 민감 정보는 응답에서 제거:
```java
public UserDTO signIn(UserDTO userDTO) {
    validateSignIn(userDTO);
    return userService.findById(userDTO.getUserId()).removePass();
}

// DTO에 헬퍼 메서드 추가
public UserDTO removePass() {
    this.password = null;
    this.passwordSalt = null;
    return this;
}
```

## 테스트 코드 작성 규칙

### 1. 테스트 클래스 구조

```java
@SpringBootTest
@TestPropertySource(properties = {
    "설정키=설정값"
})
class DomainServiceTest {

    @Autowired
    private DomainService domainService;

    @Test
    void 테스트메서드명_한글가능() {
        // given: 테스트 데이터 준비

        // when: 테스트 실행

        // then: 결과 검증 (AssertJ 사용)
    }
}
```

### 2. AssertJ 사용

```java
// 기본 검증
assertThat(result).isNotNull();
assertThat(result).isEqualTo(expected);
assertThat(list).hasSize(10);

// 예외 검증
assertThatThrownBy(() -> service.method())
    .isInstanceOf(UserException.class)
    .hasMessageContaining("예외 메시지");

// Boolean 검증
assertThat(result).isTrue();
assertThat(result).isFalse();
```

### 3. 테스트 네이밍

- 메서드명: 한글 사용 가능
- Given-When-Then 패턴 사용
- 예: `비밀번호_다른_경우()`, `사용자_생성_성공()`

### 4. 테스트 범위

각 도메인 모듈당 다음 테스트 작성:
1. **Service 테스트**: 비즈니스 로직 검증
2. **Repository 테스트**: DB 조회 로직 검증 (선택)
3. **Controller 테스트**: API 엔드포인트 검증 (선택)

## 모듈 개발 체크리스트

새로운 모듈을 개발할 때 다음 순서로 진행:

- [ ] 1. 도메인 엔티티 작성 (`domain/`)
- [ ] 2. DTO 작성 (`dto/`)
- [ ] 3. Repository 인터페이스 작성 (`repository/`)
- [ ] 4. Mapper 인터페이스 작성 (`mapper/`)
- [ ] 5. 예외 클래스 작성 (`exception/`)
- [ ] 6. 예외 메시지 상수 작성 (`constant/`)
- [ ] 7. Service 작성 (`service/`)
- [ ] 8. Controller 작성 (`web/`)
- [ ] 9. Service 단위 테스트 작성 (`test/`)
- [ ] 10. API 통합 테스트 작성 (선택)

## 예시: 게시글(Post) 모듈 개발

### 1. Entity
```java
package com.wan.framework.post.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_post")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Column(name = "data_code")
    @Enumerated(EnumType.STRING)
    private DataStateCode dataCode;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.dataCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }
}
```

### 2. DTO
```java
package com.wan.framework.post.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private String authorId;
    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;
    private DataStateCode dataCode;
}
```

### 3. Repository
```java
package com.wan.framework.post.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndDataCodeNot(Long id, DataStateCode dataCode);
    Page<Post> findAllByDataCodeNot(Pageable pageable, DataStateCode dataCode);
    boolean existsByTitleAndDataCodeNot(String title, DataStateCode dataCode);
}
```

### 4. Mapper
```java
package com.wan.framework.post.mapper;

import com.wan.framework.post.domain.Post;
import com.wan.framework.post.dto.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostDTO toDto(Post post);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "modifiedTime", ignore = true)
    Post toEntity(PostDTO dto);
}
```

### 5. Exception
```java
package com.wan.framework.post.exception;

import com.wan.framework.base.exception.ExceptionConst;
import com.wan.framework.base.exception.FrameworkException;

public class PostException extends FrameworkException {
    public PostException(ExceptionConst exceptionConst) {
        super(exceptionConst);
    }
}
```

### 6. Exception Message
```java
package com.wan.framework.post.constant;

import com.wan.framework.base.exception.ExceptionConst;

public enum PostExceptionMessage implements ExceptionConst {
    NOT_FOUND("POST_001", "게시글을 찾을 수 없습니다"),
    INVALID_TITLE("POST_002", "제목이 유효하지 않습니다"),
    DUPLICATE_TITLE("POST_003", "이미 존재하는 제목입니다");

    private final String code;
    private final String message;

    PostExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

### 7. Service
```java
package com.wan.framework.post.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.post.dto.PostDTO;
import com.wan.framework.post.domain.Post;
import com.wan.framework.post.exception.PostException;
import com.wan.framework.post.mapper.PostMapper;
import com.wan.framework.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.post.constant.PostExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        if (postRepository.existsByTitleAndDataCodeNot(postDTO.getTitle(), D)) {
            throw new PostException(DUPLICATE_TITLE);
        }

        Post post = postMapper.toEntity(postDTO);
        Post saved = postRepository.save(post);
        return postMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PostDTO findById(Long id) {
        Post post = postRepository.findByIdAndDataCodeNot(id, D)
            .orElseThrow(() -> new PostException(NOT_FOUND));
        return postMapper.toDto(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> findAll(Pageable pageable) {
        return postRepository.findAllByDataCodeNot(pageable, D)
            .map(postMapper::toDto);
    }

    @Transactional
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findByIdAndDataCodeNot(id, D)
            .orElseThrow(() -> new PostException(NOT_FOUND));

        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setDataCode(DataStateCode.U);

        return postMapper.toDto(postRepository.save(post));
    }

    @Transactional
    public PostDTO deletePost(Long id) {
        Post post = postRepository.findByIdAndDataCodeNot(id, D)
            .orElseThrow(() -> new PostException(NOT_FOUND));

        post.setDataCode(D);
        return postMapper.toDto(postRepository.save(post));
    }
}
```

### 8. Controller
```java
package com.wan.framework.post.web;

import com.wan.framework.post.dto.PostDTO;
import com.wan.framework.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        PostDTO created = postService.createPost(postDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        PostDTO post = postService.findById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public Page<PostDTO> getPostList(
        @RequestParam(value = "page", defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return postService.findAll(pageRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
        @PathVariable Long id,
        @RequestBody PostDTO postDTO) {

        PostDTO updated = postService.updatePost(id, postDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 9. Service Test
```java
package com.wan.framework.post.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.post.dto.PostDTO;
import com.wan.framework.post.exception.PostException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.post.constant.PostExceptionMessage.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Test
    void 게시글_생성_성공() {
        // given
        PostDTO postDTO = PostDTO.builder()
            .title("테스트 제목")
            .content("테스트 내용")
            .authorId("user1")
            .build();

        // when
        PostDTO created = postService.createPost(postDTO);

        // then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("테스트 제목");
        assertThat(created.getDataCode()).isEqualTo(DataStateCode.I);
    }

    @Test
    void 게시글_조회_성공() {
        // given
        PostDTO postDTO = createTestPost("조회 테스트", "내용");

        // when
        PostDTO found = postService.findById(postDTO.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("조회 테스트");
    }

    @Test
    void 존재하지_않는_게시글_조회시_예외발생() {
        // when & then
        assertThatThrownBy(() -> postService.findById(999L))
            .isInstanceOf(PostException.class)
            .hasMessageContaining(NOT_FOUND.getMessage());
    }

    @Test
    void 게시글_수정_성공() {
        // given
        PostDTO original = createTestPost("원본 제목", "원본 내용");
        PostDTO updateDTO = PostDTO.builder()
            .title("수정된 제목")
            .content("수정된 내용")
            .build();

        // when
        PostDTO updated = postService.updatePost(original.getId(), updateDTO);

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        assertThat(updated.getDataCode()).isEqualTo(DataStateCode.U);
    }

    @Test
    void 게시글_삭제_성공() {
        // given
        PostDTO post = createTestPost("삭제할 게시글", "내용");

        // when
        PostDTO deleted = postService.deletePost(post.getId());

        // then
        assertThat(deleted.getDataCode()).isEqualTo(DataStateCode.D);

        // 삭제된 게시글 조회 시 예외 발생
        assertThatThrownBy(() -> postService.findById(post.getId()))
            .isInstanceOf(PostException.class);
    }

    @Test
    void 게시글_목록_조회_페이징() {
        // given
        createTestPost("제목1", "내용1");
        createTestPost("제목2", "내용2");
        createTestPost("제목3", "내용3");

        // when
        Page<PostDTO> page = postService.findAll(PageRequest.of(0, 10));

        // then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void 중복된_제목으로_생성시_예외발생() {
        // given
        createTestPost("중복 제목", "내용");

        PostDTO duplicate = PostDTO.builder()
            .title("중복 제목")
            .content("다른 내용")
            .authorId("user2")
            .build();

        // when & then
        assertThatThrownBy(() -> postService.createPost(duplicate))
            .isInstanceOf(PostException.class)
            .hasMessageContaining(DUPLICATE_TITLE.getMessage());
    }

    // 헬퍼 메서드
    private PostDTO createTestPost(String title, String content) {
        PostDTO postDTO = PostDTO.builder()
            .title(title)
            .content(content)
            .authorId("testUser")
            .build();
        return postService.createPost(postDTO);
    }
}
```

## 주의사항

1. **MapStruct 빌드**: Mapper 인터페이스 작성 후 반드시 빌드 필요
   ```bash
   ./gradlew clean build
   ```

2. **테스트 격리**: `@Transactional`로 각 테스트가 독립적으로 실행되도록 보장

3. **예외 메시지**: 사용자에게 노출되는 메시지이므로 명확하게 작성

4. **로깅**: 중요한 비즈니스 로직 전후로 로그 추가
   ```java
   log.info("게시글 생성 시작: {}", postDTO.getTitle());
   log.debug("게시글 상세 정보: {}", postDTO);
   ```

5. **null 체크**: 필수 필드는 엔티티에 `nullable = false` 설정

## 참고 자료

- 기존 구현 모듈: `user`, `program`, `menu`, `board`, `history`
- Spring Data JPA 문서: https://spring.io/projects/spring-data-jpa
- MapStruct 문서: https://mapstruct.org/
- AssertJ 문서: https://assertj.github.io/doc/
