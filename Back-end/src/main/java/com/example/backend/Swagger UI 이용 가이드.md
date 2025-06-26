# Swagger UI 사용 가이드

## 1. Swagger UI 접속
브라우저에서 다음 주소로 접속합니다:
```
http://localhost:8080/swagger-ui/index.html
```

## 2. JWT 토큰 인증 설정

### 2.1 로그인하여 토큰 발급받기
1. **UserAPI** 섹션을 찾습니다
2. `POST /user/login` 엔드포인트를 클릭합니다
3. **Try it out** 버튼을 클릭합니다
4. Request body에 로그인 정보를 입력합니다:
   ```json
   {
     "email": "test@test.com",
     "password": "test1234"
   }
   ```
5. **Execute** 버튼을 클릭합니다
6. Response body에서 `accessToken`을 복사합니다

### 2.2 토큰 등록하기
1. Swagger UI 상단의 **Authorize** 버튼을 클릭합니다 (자물쇠 아이콘)
2. **Value** 입력란에 다음 형식으로 토큰을 입력합니다:
   ```
   Bearer [복사한 accessToken]
   ```
   예시:
   ```
   Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1NTBl...
   ```
3. **Authorize** 버튼을 클릭합니다
4. **Close** 버튼을 클릭합니다

## 3. 장바구니 API 테스트

### 3.1 장바구니 조회
1. **CartAPI** 섹션에서 `GET /cart` 엔드포인트를 클릭합니다
2. **Try it out** → **Execute** 클릭
3. 현재 사용자의 장바구니 정보가 반환됩니다

### 3.2 투어 검색
1. `GET /tour/search` 엔드포인트를 클릭합니다
2. **Try it out** 클릭
3. 검색 파라미터를 입력합니다:
    - `keyword`: 검색어 (예: "해운대")
    - `region`: 지역 (예: "부산")
    - `category`: 카테고리 (예: "관광지")
    - `page`: 페이지 번호 (기본값: 0)
    - `size`: 페이지 크기 (기본값: 20)
4. **Execute** 클릭

### 3.3 장바구니에 투어 추가
1. `POST /cart/tours` 엔드포인트를 클릭합니다
2. **Try it out** 클릭
3. Request body 예시:
   ```json
   {
     "longitude": 129.1603,
     "latitude": 35.1595,
     "address": "부산광역시 해운대구 해운대해변로 264",
     "image": "https://example.com/haeundae.jpg",
     "tema": "해변"
   }
   ```
4. **Execute** 클릭

## 4. 인증이 필요한 API 표시
- 자물쇠 아이콘이 있는 API는 JWT 토큰이 필요합니다
- 자물쇠 아이콘이 없는 API는 인증 없이 사용 가능합니다

## 5. 토큰 만료 시
토큰이 만료되면 401 Unauthorized 에러가 발생합니다.
이때는 다시 로그인하여 새로운 토큰을 발급받아 사용하세요.

## 6. 문제 해결
- **401 Unauthorized**: 토큰이 없거나 만료됨 → 다시 로그인
- **403 Forbidden**: 권한 부족 → 올바른 권한의 계정으로 로그인
- **500 Internal Server Error**: 서버 오류 → 로그 확인 필요