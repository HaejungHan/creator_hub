<%--
  Created by IntelliJ IDEA.
  User: guro13
  Date: 24. 11. 20.
  Time: 오후 8:34
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page pageEncoding="UTF-8" %>

<div class="dashboard">
  <div class="sidebar">
    <div class="logo">
      <i class="fab fa-youtube"></i>
      <span>Creator_hub</span>
    </div>

    <div class="menu-item active" data-category="home">
      <i class="fas fa-home"></i>
      <span>홈</span>
    </div>

    <div class="menu-item" data-category="popular">
      <i class="fas fa-fire"></i>
      <span>인기 동영상</span>
    </div>

    <div class="menu-item" data-category="recommended">
      <i class="fas fa-star"></i>
      <span>추천 동영상</span>
    </div>

    <button class="auth-button-side login-btn" id="loginBtn">
      <i class="fas fa-sign-in-alt"></i>
      <span>로그인</span>
    </button>
  </div>
</div>
<script>
  function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
      const cookieValue = parts.pop().split(';').shift();
      console.log("Raw cookie value:", cookieValue);  // 쿠키 값 확인 (디버깅용)

      // "Bearer " 접두어를 제거하고 실제 토큰 반환
      return cookieValue.replace(/^Bearer\s/, '');  // 'Bearer ' 제거
    }
    return '';
  }

  document.addEventListener('DOMContentLoaded', function() {
    // 로그인 버튼을 찾기
    const loginBtn = document.getElementById('loginBtn');

    // 로그인 버튼이 존재하면 클릭 이벤트 리스너 추가
    if (loginBtn) {
      loginBtn.addEventListener('click', function(event) {
        if (getCookie('access_token')) {
          // 로그아웃 처리
          document.cookie = "access_token=; path=/; secure; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT;";
          document.cookie = "refresh_token=; path=/; secure; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT;";
          updateAuthButton();  // 버튼 상태 업데이트
          alert('로그아웃 성공!');
        } else {
          // 로그인 모달 열기
          document.getElementById('loginModal').style.display = 'block';
          document.body.style.overflow = 'hidden';  // 페이지 스크롤 잠금
        }
      });
    }

    // 페이지 로드 시 로그인 상태에 맞게 버튼 상태 변경
    updateAuthButton();
  });

  // 로그인 상태에 맞게 버튼 업데이트
  function updateAuthButton() {
    const loginBtn = document.getElementById('loginBtn');
    const accessToken = getCookie('access_token');
    console.log('Access Token:', accessToken);  // 콘솔에서 access_token 확인

    // 쿠키에 access_token이 있으면 로그인 상태
    if (accessToken) {
      loginBtn.innerHTML = '<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>';
      loginBtn.setAttribute('id', 'logoutBtn');  // ID 변경
    } else {
      loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i><span>로그인</span>';
      loginBtn.setAttribute('id', 'loginBtn');  // ID 변경
    }
  }
</script>
