<%--
  Created by IntelliJ IDEA.
  User: guro13
  Date: 24. 11. 21.
  Time: 오후 8:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<!-- 로그인 모달 -->
<<div class="modal" id="loginModal" style="display:none;">
<div class="modal-content">
    <span class="close-modal" data-modal="loginModal">&times;</span>
    <div class="modal-header">
        <h2>로그인</h2>
        <p>Creator_hub에 오신 것을 환영합니다</p>
    </div>
    <form id="loginForm">
        <div class="form-group">
            <label for="email">이메일</label>
            <input type="email" id="email" name="email" placeholder="이메일을 입력하세요" required>
        </div>

        <div class="form-group">
            <label for="password">비밀번호</label>
            <input type="password" id="password" name="password" placeholder="비밀번호를 입력하세요" required>
        </div>
        <button type="submit" class="modal-submit">로그인</button>
    </form>
    <div class="modal-footer">
        <p>계정이 없으신가요? <button id="showSignupBtn" class="secondary-button">회원가입</button></p>
    </div>
</div>
</div>

<div class="modal" id="signupModal" style="display:none;">
    <div class="modal-content">
        <span class="close-modal" data-modal="signupModal">&times;</span>
        <div class="modal-header">
            <h2>회원가입</h2>
            <p>크리에이터 스튜디오 회원이 되어보세요</p>
        </div>
        <form class="modal-form" id="signupForm">
            <div class="form-group">
                <label for="signupEmail">이메일</label>
                <input type="email" id="signupEmail" required>
            </div>
            <div class="form-group">
                <label for="signupPassword">비밀번호</label>
                <input type="password" id="signupPassword" required>
            </div>
            <div class="form-group">
                <label for="signupPasswordConfirm">비밀번호 확인</label>
                <input type="password" id="signupPasswordConfirm" required>
            </div>
            <div class="form-group">
                <label>관심사 (3개 선택)</label>
                <div class="interests-grid">
                    <label class="interest-item"><input type="checkbox" name="interests" value="SPORTS"> 스포츠</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="MUSIC"> 음악</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="STUDY"> 공부</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="ART"> 예술</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="TRAVEL"> 여행</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="MOVIES"> 영화</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="FOOD"> 음식</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="READING"> 독서</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="GAMING"> 게임</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="FASHION"> 패션</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="SHOPPING"> 쇼핑</label>
                    <label class="interest-item"><input type="checkbox" name="interests" value="IT_DEV"> 개발</label>
                </div>
                <p id="interestError" class="error-message" style="display: none; color: #EF4444;">관심사를 3개 선택해주세요</p>
            </div>
            <button type="submit" class="modal-submit">가입하기</button>
        </form>
        <div class="modal-footer">
            <p>이미 계정이 있으신가요?</p>
            <button id="showLoginBtn" class="secondary-button">로그인</button>
        </div>
    </div>
</div>

<!-- JavaScript -->
<script>
    const signupModal = document.getElementById('signupModal');
    const showSignupBtn = document.getElementById('showSignupBtn');
    const loginModal = document.getElementById('loginModal');
    const loginBtn = document.getElementById('loginBtn');
    const showLoginBtn = document.getElementById('showLoginBtn');
    const closeButtons = document.querySelectorAll('.close-modal');
    const interestError = document.getElementById('interestError'); // 관심사 오류 메시지

    function handleLogin(event) {
        event.preventDefault();  // 폼 제출 기본 동작 방지

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // 이메일과 비밀번호가 비어있는지 확인
        if (!email || !password) {
            alert('이메일과 비밀번호는 필수 입력 사항입니다.');
            return;
        }

        fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: email,
                password: password,
            }),
            credentials: 'include',  // 쿠키 포함
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorData => {
                        throw new Error(errorData.message);
                    });
                }

                loginModal.style.display = 'none';
                document.body.style.overflow = 'auto'; // 페이지 스크롤 활성화

                alert('로그인 성공!');
                window.location.reload();
            })
            .catch(error => {
                console.error('로그인 실패:', error);
                alert('로그인에 실패했습니다. 다시 시도해주세요.');
            });
    }

    // 로그인 폼 제출 이벤트
    document.getElementById('loginForm').addEventListener('submit', handleLogin);

    // 로그인 버튼 클릭 시 로그인 모달 열기
    loginBtn.addEventListener('click', () => {
        loginModal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    });

    // 회원가입 버튼 클릭 시 로그인 모달을 회원가입 모달로 변경
    showSignupBtn.addEventListener('click', () => {
        loginModal.style.display = 'none';
        signupModal.style.display = 'block';
    });

    // 회원가입 폼 처리
    function handleSignup(event) {
        event.preventDefault();  // 폼 제출 기본 동작 방지

        const email = document.getElementById('signupEmail').value;
        const password = document.getElementById('signupPassword').value;
        const passwordConfirm = document.getElementById('signupPasswordConfirm').value;

        // 비밀번호 확인이 일치하는지 체크
        if (password !== passwordConfirm) {
            alert('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 선택된 관심사들을 배열로 가져오기
        const selectedInterests = Array.from(document.querySelectorAll('input[name="interests"]:checked'))
            .map(input => input.value);

        // 관심사를 3개 선택했는지 확인
        if (selectedInterests.length !== 3) {
            interestError.style.display = 'block';
            return;
        } else {
            interestError.style.display = 'none';
        }

        // 회원가입 요청 보내기
        fetch('/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: email,
                password: password,
                interests: selectedInterests
            })
        })
            .then(response => {
                if (!response.ok) {
                    // 서버에서 응답한 JSON 메시지를 처리
                    return response.json().then(errorData => {
                        throw new Error(errorData.message);  // 예외 처리
                    });
                }
                return response.json();  // 정상적인 응답일 경우
            })
            .then(data => {
                console.log('회원가입 성공:', data);
                if (data.message === "회원가입이 완료되었습니다.") {
                    alert('회원가입이 완료되었습니다!');
                    signupModal.style.display = 'none';
                    loginModal.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('회원가입 실패:', error);
                alert('회원가입 실패! 다시 시도해주세요.');
            });
    }

    // 회원가입 폼 제출 이벤트
    document.getElementById('signupForm').addEventListener('submit', handleSignup);

    // 로그인 버튼 클릭 시 회원가입 모달을 로그인 모달로 변경
    showLoginBtn.addEventListener('click', () => {
        signupModal.style.display = 'none';
        loginModal.style.display = 'block';
    });

    // 모달 닫기 버튼 이벤트 리스너
    closeButtons.forEach(button => {
        button.addEventListener('click', () => {
            const modalId = button.getAttribute('data-modal');
            document.getElementById(modalId).style.display = 'none';
            document.body.style.overflow = 'auto';
        });
    });

    // 배경 클릭 시 모달 닫기
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    });
</script>



