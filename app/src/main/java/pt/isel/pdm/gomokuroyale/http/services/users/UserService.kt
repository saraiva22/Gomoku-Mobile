package pt.isel.pdm.gomokuroyale.http.services.users

import com.google.gson.Gson
import okhttp3.OkHttpClient
import pt.isel.pdm.gomokuroyale.http.services.HTTPService
import pt.isel.pdm.gomokuroyale.http.services.users.dto.UserCreateInputModel
import pt.isel.pdm.gomokuroyale.http.services.users.dto.UserCreateTokenInputModel
import pt.isel.pdm.gomokuroyale.http.services.users.models.GetRankingOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.GetStatsOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.GetUserHomeOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.GetUserOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.LoginOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.LogoutOutput
import pt.isel.pdm.gomokuroyale.http.services.users.models.RegisterOutput
import pt.isel.pdm.gomokuroyale.http.utils.Uris

//TODO: CHANGE OUTPUT MODELS TO DOMAIN MODELS
class UserService(
    client: OkHttpClient,
    gson: Gson,
    apiEndpoint: String
) : HTTPService(client, gson, apiEndpoint) {
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): RegisterOutput =
        post(path = Uris.Users.createUser(), body = UserCreateInputModel(username, email, password))


    suspend fun login(username: String, password: String): LoginOutput =
        post(path = Uris.Users.token(), body = UserCreateTokenInputModel(username, password))

    suspend fun logout(token: String): LogoutOutput =
        post(path = Uris.Users.logout(), token = token,)

    suspend fun getUser(id: Int): GetUserOutput =
        get(path = Uris.Users.getUserById(id))

    suspend fun getAuthHome(token: String): GetUserHomeOutput =
        get(path = Uris.Users.authHome(), token = token,)

    suspend fun getStatsById(id: Int, token: String): GetStatsOutput =
        get(path = Uris.Users.getStatsById(id), token = token,)

    suspend fun getRankingInfo(page: Int): GetRankingOutput =
        get(path = Uris.Users.rankingInfo() + "?page=" + page)

}
