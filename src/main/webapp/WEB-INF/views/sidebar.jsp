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

    <a href="${pageContext.request.contextPath}/home">
    <div class="menu-item active" data-category="home">
      <i class="fas fa-home"></i>
      <span>홈</span>
    </div>
    </a>

    <a href="${pageContext.request.contextPath}/popular">
    <div class="menu-item" data-category="popular">
      <i class="fas fa-fire"></i>
      <span>인기 동영상 TOP 10</span>
    </div>
    </a>

      <a href="${pageContext.request.contextPath}/recommendations">
    <div class="menu-item" data-category="recommended">
      <i class="fas fa-star"></i>
      <span>추천 동영상</span>
    </div>
      </a>

    <button class="auth-button-side login-btn" id="loginBtn">
      <i class="fas fa-sign-in-alt"></i>
      <span>로그인</span>
    </button>

  </div>
</div>
<script>
  function getCookie(name) {
    const value = document.cookie;
    const parts = value.split('; ');
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      const [cookieName, cookieValue] = part.split('=');

      if (cookieName === name) {
        console.log("Raw cookie value:", cookieValue);
        return cookieValue.replace(/^Bearer\s/, '');
      }
    }
    return '';
  }

  document.addEventListener('DOMContentLoaded', function() {
    const loginBtn = document.getElementById('loginBtn');

    if (loginBtn) {
      loginBtn.addEventListener('click', function(event) {
        if (getCookie('access_token')) {
          // 로그아웃 처리
          deleteCookie('access_token');
          deleteCookie('refresh_token');

          // 홈 화면으로 리디렉션
          window.location.href = "${pageContext.request.contextPath}/home";
        } else {
          // 로그인 모달 표시
          document.getElementById('loginModal').style.display = 'block';
          document.body.style.overflow = 'hidden';
        }
      });
    }

    updateAuthButton();
  });

  function deleteCookie(name) {
    document.cookie = name + "=; path=/; SameSite=Lax; expires=Thu, 01 Jan 2024 00:00:00 GMT;";
  }

  function updateAuthButton() {
    const loginBtn = document.getElementById('loginBtn');
    const accessToken = getCookie('access_token');
    console.log('Access Token:', accessToken);

    if (loginBtn) {
      if (accessToken) {
        loginBtn.innerHTML = '<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>';
        loginBtn.setAttribute('id', 'logoutBtn');
      } else {
        loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i><span>로그인</span>';
        loginBtn.setAttribute('id', 'loginBtn');
      }
    }
  }
</script>
