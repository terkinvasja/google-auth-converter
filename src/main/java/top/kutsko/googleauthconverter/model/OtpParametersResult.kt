package top.kutsko.googleauthconverter.model

data class OtpParametersResult(
        val secret: String,
        val name: String,
        val issuer: String
)
