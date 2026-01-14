package com.KimZo2.Back.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_001", "서버 내부 오류가 발생했습니다."),

    // Common Exception
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 유효하지 않습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "COMMON_002", "페이지 번호나 크기가 올바르지 않습니다. (size는 최대 6)"),

    // spring security exception
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SECURITY_001", "접근권한이 없습니다."),
    NOT_LOGIN_USER(HttpStatus.FORBIDDEN, "SECURITY_002", "로그인하지 않은 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "SECURITY_003", "비밀번호가 일치하지 않습니다."),

    // jwt token exception
    EMPTY_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "JWT_001", "토큰이 비어있습니다."),
    MALFORMED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "JWT_002", "잘못된 JWT 형식입니다."),
    UNSUPPORTED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "JWT_003", "지원하지 않는 JWT입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_004", "만료된 토큰입니다."),
    TOKEN_PARSING_FAILED(HttpStatus.UNAUTHORIZED, "JWT_005", "유효하지 않은 토큰 (파싱 실패)"),
    INVALID_TOKEN_SIGNATURE(HttpStatus.FORBIDDEN, "JWT_006", "JWT 서명 검증에 실패했습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "JWT_007", "유효하지 않은 토큰 형식입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_005", "유효하지 않은 RefreshToken입니다"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_005", "유효하지 않은 AccessToken입니다"),

    // member exception
    INVALID_AVATAR_NUM(HttpStatus.BAD_REQUEST, "MEMBER_001", "아바타 형식이 올바르지 않습니다"),

    // auth exception
    ADDITIONAL_SIGNUP_REQUIRED(HttpStatus.PRECONDITION_REQUIRED, "AUTH_001", "추가 회원가입 정보가 필요합니다."),
    DUPLICATE_USERID(HttpStatus.CONFLICT, "AUTH_002", "이미 존재하는 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "AUTH_003", "이미 존재하는 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_004", "존재하지 않는 사용자입니다."),
    TOKEN_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005", "토큰(Code) 요청에 실패했습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "AUTH_005", "이미 가입된 회원입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_006", "cookie에 저장된 refreshToken이 없습니다."),


    // signup exception
    INVALID_SIGNUP_REQUEST(HttpStatus.BAD_REQUEST, "SIGNUP_001", "회원가입 요청 정보가 올바르지 않습니다."), // 포괄적
    AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "SIGNUP_002", "약관 동의는 필수입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "SIGNUP_003", "닉네임 형식이 올바르지 않습니다."),
    INVALID_BIRTHDAY_FORMAT(HttpStatus.BAD_REQUEST, "SIGNUP_004", "생년월일 형식이 올바르지 않습니다."),

    // room exception
    DUPLICATE_ROOM_NAME(HttpStatus.CONFLICT, "ROOM_001", "이미 존재하는 방 이름입니다."),
    PASSWORD_REQUIRED_FOR_PRIVATE_ROOM(HttpStatus.BAD_REQUEST, "ROOM_002", "비공개 방 생성 시 비밀번호는 필수입니다."),
    ROOM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ROOM_003", "방 생성에 실패했습니다. (저장 오류)"),
    ROOM_LIST_FETCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ROOM_004", "방 목록을 불러오는 중 오류가 발생했습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_005", "존재하지 않는 방입니다."),

    // participation / websocket exception
    ROOM_NOT_FOUND_OR_EXPIRED(HttpStatus.NOT_FOUND, "JOIN_001", "방을 찾을 수 없거나 이미 만료된 방입니다."),
    ROOM_CAPACITY_FULL(HttpStatus.CONFLICT, "JOIN_002", "방 정원이 가득 찼습니다."),
    INVALID_ROOM_PASSWORD(HttpStatus.BAD_REQUEST, "JOIN_003", "방 비밀번호가 일치하지 않습니다."),

    // chat exception
    ROOM_ID_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT_001", "방 ID는 필수입니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT_002", "닉네임은 필수입니다."),
    CHAT_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "CHAT_003", "채팅 내용은 필수입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "CHAT_004", "유효하지 않은 사용자 ID입니다."),

    // subscribe exception
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "JOIN_004", "해당 방에 입장할 권한이 없습니다."),
    SOCKET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "JOIN_005", "접근 권한이 없습니다."),
    INVALID_DESTINATION(HttpStatus.BAD_REQUEST, "JOIN_006", "잘못된 요청 경로입니다.");

    private final HttpStatus status;    // HTTP 상태
    private final String code;          // API 응답에 사용할 커스텀 에러 코드 (HTTP 상태 코드와 동일하게)
    private final String message;       // API 응답에 사용할 에러 메시지
}
