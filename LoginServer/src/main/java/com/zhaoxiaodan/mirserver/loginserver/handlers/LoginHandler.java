package com.zhaoxiaodan.mirserver.loginserver.handlers;

import com.zhaoxiaodan.mirserver.db.entities.ServerInfo;
import com.zhaoxiaodan.mirserver.db.entities.User;
import com.zhaoxiaodan.mirserver.network.packets.ClientPacket;
import com.zhaoxiaodan.mirserver.network.packets.ServerPacket;
import com.zhaoxiaodan.mirserver.network.Handler;
import com.zhaoxiaodan.mirserver.network.Session;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

public class LoginHandler extends Handler {

	@Override
	public void onPacket(ClientPacket packet) throws Exception {
		ClientPacket.Login loginRequest = (ClientPacket.Login) packet;

		List<User> list = session.db.query(User.class, Restrictions.eq("loginId", loginRequest.user.loginId));
		if (1 != list.size()) {
			session.sendPacket(new ServerPacket.LoginFail(ServerPacket.LoginFail.Reason.UserNotFound));
			return;
		} else {
			User user = list.get(0);
			if (user.password.equals(loginRequest.user.password)) {

				user.lastLoginTime = new Date();
				session.db.update(user);
				session.put("user", user);
				logger.info("user {} login, login user count:{}", user.loginId, Session.size());

				List<ServerInfo> serverInfoList = session.db.query(ServerInfo.class);
				session.sendPacket(new ServerPacket.LoginSuccSelectServer(serverInfoList));
			} else {
				session.sendPacket(new ServerPacket.LoginFail(ServerPacket.LoginFail.Reason.WrongPwd));
			}
		}
	}

}
