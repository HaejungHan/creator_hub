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
          document.cookie = "access_token=; path=/; secure; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT;";
          document.cookie = "refresh_token=; path=/; secure; SameSite=Strict; expires=Thu, 01 Jan 1970 00:00:00 GMT;";
          updateAuthButton();
          alert('로그아웃 성공!');
        } else {
          document.getElementById('loginModal').style.display = 'block';
          document.body.style.overflow = 'hidden';
        }
      });
    }

    updateAuthButton();
  });

  function updateAuthButton() {
    const loginBtn = document.getElementById('loginBtn');
    const accessToken = getCookie('access_token');
    console.log('Access Token:', accessToken);

    if (accessToken) {
      loginBtn.innerHTML = '<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>';
      loginBtn.setAttribute('id', 'logoutBtn');
    } else {
      loginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i><span>로그인</span>';
      loginBtn.setAttribute('id', 'loginBtn');
    }
  }
</script>
