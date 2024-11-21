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
      <span>Creator Studio</span>
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



