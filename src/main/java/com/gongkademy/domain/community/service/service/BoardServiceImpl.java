package com.gongkademy.domain.community.service.service;

import com.gongkademy.domain.community.service.dto.response.BoardResponseDTO;
import com.gongkademy.domain.community.common.entity.board.Board;
import com.gongkademy.domain.community.common.entity.board.BoardType;
import com.gongkademy.domain.community.common.entity.pick.Pick;
import com.gongkademy.domain.community.common.entity.pick.PickType;
import com.gongkademy.domain.community.common.repository.BoardRepository;
import com.gongkademy.domain.community.common.repository.PickRepository;
import com.gongkademy.domain.member.entity.Member;
import com.gongkademy.domain.member.repository.MemberRepository;
import com.gongkademy.global.exception.CustomException;
import com.gongkademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final PickRepository pickRepository;

    // 최신 순 매직넘버 시작
    private final int DEFAULT_TOP = 0;

    @Override
    public BoardResponseDTO getBoard(Long id, Long memberId) {
        Board board = boardRepository.findByIdWithComments(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD_ID));

        boolean isLiked = (memberId != null) && isLikedByMember(board, memberId);
        boolean isScrapped = (memberId != null) && isScrappedByMember(board, memberId);

        board.setHit(board.getHit() + 1);

        return convertToDTOWithPicks(board, isLiked, isScrapped);
    }

    @Override
    public BoardResponseDTO getNotLoginBoard(Long id) {
        Board board = boardRepository.findByIdWithComments(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD_ID));
        board.setHit(board.getHit() + 1);

        return convertToDTO(board);
    }

    @Override
    public List<BoardResponseDTO> getLatestBoards(int LIMIT, Long memberId) {
        Pageable pageable = PageRequest.of(DEFAULT_TOP, LIMIT);
        List<Board> boards = boardRepository.findByBoardTypeOrderByCreateTimeDesc(BoardType.NOTICE,pageable).getContent();

        return boards.stream()
                .map(board -> {
                    boolean isLiked = (memberId != null) && isLikedByMember(board, memberId);
                    boolean isScraped = (memberId != null) && isScrappedByMember(board, memberId);
                    return convertToDTOWithPicks(board, isLiked, isScraped);
                }).collect(Collectors.toList());
    }

    @Override
    public List<BoardResponseDTO> getNotLoginLatestBoards(int index) {
        Pageable pageable = PageRequest.of(DEFAULT_TOP, index);
        return boardRepository.findByBoardTypeOrderByCreateTimeDesc(BoardType.NOTICE, pageable).getContent()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void toggleLikeBoard(Long articleId, Long memberId) {
        Board board = boardRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD_ID));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));

        Optional<Pick> pickOptional = pickRepository.findByBoardAndMemberAndPickType(board, member, PickType.LIKE);

        if (pickOptional.isPresent()) {
            pickRepository.delete(pickOptional.get());
            board.setLikeCount(board.getLikeCount() - 1);
        } else {
            Pick pick = new Pick(board, member, PickType.LIKE);
            pickRepository.save(pick);
            board.setLikeCount(board.getLikeCount() + 1);
        }

        boardRepository.save(board);
    }

    @Override
    public void toggleScrapBoard(Long articleId, Long memberId) {
        Board board = boardRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_BOARD_ID));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));

        Optional<Pick> pickOptional = pickRepository.findByBoardAndMemberAndPickType(board, member, PickType.SCRAP);

        if (pickOptional.isPresent()) {
            pickRepository.delete(pickOptional.get());
            board.setScrapCount(board.getScrapCount() - 1);
        } else {
            Pick pick = new Pick(board, member, PickType.SCRAP);
            pickRepository.save(pick);
            board.setScrapCount(board.getScrapCount() + 1);
        }

        boardRepository.save(board);
    }

    // 좋아요한 게시글
    @Override
    public List<BoardResponseDTO> getLikeBoards(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));

        return pickRepository.findAllByMemberAndPickType(member, PickType.LIKE)
                .stream().map(Pick::getBoard).map(this::convertToDTO).collect(Collectors.toList());
    }

    // 스크랩한 게시글
    @Override
    public List<BoardResponseDTO> getScrapBoards(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));

        return pickRepository.findAllByMemberAndPickType(member, PickType.SCRAP)
                .stream().map(Pick::getBoard).map(this::convertToDTO).collect(Collectors.toList());

    }

    private boolean isLikedByMember(Board board, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));
        Optional<Pick> pickOptional = pickRepository.findByBoardAndMemberAndPickType(board, member, PickType.LIKE);
        return pickOptional.isPresent();
    }

    private boolean isScrappedByMember(Board board, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_MEMBER_ID));
        Optional<Pick> pickOptional = pickRepository.findByBoardAndMemberAndPickType(board, member, PickType.SCRAP);
        return pickOptional.isPresent();
    }

}
