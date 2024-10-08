package com.gongkademy.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gongkademy.domain.community.common.entity.board.Board;
import com.gongkademy.domain.community.common.entity.comment.Comment;
import com.gongkademy.domain.community.common.entity.comment.CommentLike;
import com.gongkademy.domain.community.common.entity.pick.Pick;
import com.gongkademy.domain.course.common.entity.CourseComment;
import com.gongkademy.domain.course.common.entity.CourseLike;
import com.gongkademy.domain.course.common.entity.CourseReview;
import com.gongkademy.domain.course.common.entity.RegistCourse;
import com.gongkademy.domain.course.common.entity.RegistLecture;
import com.gongkademy.domain.course.common.entity.Scrap;

import com.gongkademy.domain.member.dto.MemberSignUpDTO;
import com.gongkademy.domain.member.dto.MemberUpdateDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"memberRoleList", "picks", "comments", "commentLikes", "boards", "courseComments", "courseReviews", "registCourses", "registLectures", "scraps", "courseLikes"})
//ElementCollection으로 잡힌 애들은 toString 제외를 해줘야 Lazy 로딩이 안됨
@JsonIgnoreProperties({"HibernateLazyInitializer", "handler"})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String name;
    @NotEmpty
    private String email;
    private String nickname;

    // S3에 저장된 프로필 사진 Path
    private String profilePath;
    private LocalDate birthday;
    private Boolean agreeMarketing;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private List<MemberRole> memberRoleList = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Builder.Default
    private boolean isNotificationEnabled = true;

    @Builder.Default
    private boolean isDeleted = false;

    private LocalDateTime deletedTime;

    // 탈퇴 비즈니스 로직(soft-delete)
    public void deleteMember(String nickname) {
        this.nickname = nickname;
        this.isDeleted = true;
        this.deletedTime = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void addRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
    }

    public void changeIsNotificationEnabled() {
        this.isNotificationEnabled = !isNotificationEnabled;
    }

    public void clearRole() {
        memberRoleList.clear();
    }

    public void signup(MemberSignUpDTO memberSignUpDTO) {
        this.nickname = memberSignUpDTO.getNickname();
        this.birthday = LocalDate.parse(memberSignUpDTO.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        this.agreeMarketing = memberSignUpDTO.getAgreeMarketing();
    }

    public void update(MemberUpdateDTO memberUpdateDTO) {
        this.nickname = memberUpdateDTO.getNewNickname();
        this.agreeMarketing = memberUpdateDTO.getAgreeMarketing();
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<Pick> picks = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @Builder.Default
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<CommentLike> commentLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @Builder.Default
    @JsonManagedReference
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @Builder.Default
    @JsonManagedReference
    private List<CourseComment> courseComments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    @Builder.Default
    @JsonManagedReference
    private List<CourseReview> courseReviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<RegistCourse> registCourses = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<RegistLecture> registLectures = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<CourseLike> courseLikes = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addPick(Pick pick) {
        picks.add(pick);
        pick.setMember(this);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setMember(this);
    }

    public void addCommentLike(CommentLike commentLike) {
        commentLikes.add(commentLike);
        commentLike.setMember(this);
    }

    public void addBoard(Board board) {
        boards.add(board);
        board.setMember(this);
    }

    public void addCourseComment(CourseComment courseComment) {
        courseComments.add(courseComment);
        courseComment.setMember(this);
    }

    public void addCourseReview(CourseReview courseReview) {
        courseReviews.add(courseReview);
        courseReview.setMember(this);
    }

    public void addRegistCourse(RegistCourse registCourse) {
        registCourses.add(registCourse);
        registCourse.setMember(this);
    }

    public void addScrap(Scrap scrap) {
        scraps.add(scrap);
        scrap.setMember(this);
    }

    public void addCourseLike(CourseLike courseLike) {
        courseLikes.add(courseLike);
        courseLike.setMember(this);
    }

}