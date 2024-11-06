function checkLoginStatus() {
    const token = Cookies.get('Authorization');
    if (token) {
        $('#loginBtn').html('<i class="fas fa-sign-out-alt"></i><span>로그아웃</span>');
        $('#loginBtn').attr('id', 'logoutBtn');
    }
}

$('#loginForm').submit(function (e) {
    e.preventDefault();

    const email = $('#loginEmail').val().trim();
    const password = $('#loginPassword').val().trim();

    if (!email || !password) {
        $('#error-message').text('이메일과 비밀번호를 입력해주세요.');
        $('#error-message').show();
        return;
    }

    const data = { username: email, password: password };
    $.ajax({
        type: 'POST',
        url: '/login',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (response, textStatus, xhr) {
            const accessToken = xhr.getResponseHeader("Authorization");
            const refreshToken = xhr.getResponseHeader("RefreshToken");

            if (accessToken) {
                Cookies.set('Authorization', accessToken.split(' ')[1], { path: '/', sameSite: 'Lax', secure: false });
            }
            if (refreshToken) {
                Cookies.set('RefreshToken', refreshToken.split(' ')[1], { path: '/', sameSite: 'Lax', secure: false });
            }
            alert('로그인이 완료되었습니다.');
            window.location.href = '/main';
        },
        error: function () {
            $('#error-message').text('Invalid email or password. Please try again.');
            $('#error-message').show();
        }
    });
});

function handleLogout() {
    Cookies.remove('Authorization');
    Cookies.remove('RefreshToken');
    $('#logoutBtn').html('<i class="fas fa-sign-in-alt"></i><span>로그인</span>');
    $('#logoutBtn').attr('id', 'loginBtn');
    window.location.href = '/main';
}
