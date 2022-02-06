package me.krax.oversight

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
typealias OVERSIGHT_LOG_TYPE = Int

/**
 * Log 클래스는 메시지를 기록하고 관리하는 클래스입니다.
 * @author ruskonert
 */
open class Log
{
    // 날짜 클래스 및 시간 포맷을 저장하는 필드입니다.
    private var _dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    open fun print(logType : OVERSIGHT_LOG_TYPE, format : String, vararg args : Any) {
        when(logType) {
            OVERSIGHT_LOG_INFO -> printInfo(format, *args)
            OVERSIGHT_LOG_WARN -> printWarn(format, *args)
            OVERSIGHT_LOG_ERROR -> printError(format, *args)
            OVERSIGHT_LOG_DEBUG -> printDebug(format, *args)
        }
    }

    open fun printDebug(format : String, vararg args : Any) {
        val dateFormat = this._dtf.format(LocalDateTime.now())
        if(args.isEmpty()) println("[${dateFormat}][DEBUG] %s".format(format))
        else println("[${dateFormat}][DEBUG] %s".format(format, *args))
    }

    open fun printWarn(format : String, vararg args : Any) {
        val dateFormat = this._dtf.format(LocalDateTime.now())
        if(args.isEmpty()) println("[${dateFormat}][WARN] %s".format(format))
        else println("[${dateFormat}][WARN] %s".format(format, *args))
    }

    open fun printInfo(format : String, vararg args : Any) {
        val dateFormat = this._dtf.format(LocalDateTime.now())
        if(args.isEmpty()) println("[${dateFormat}][INFO] %s".format(format))
        else println("[${dateFormat}][INFO] %s".format(format, *args))
    }

    open fun printError(format : String, vararg args : Any) {
        val dateFormat = this._dtf.format(LocalDateTime.now())
        if(args.isEmpty()) println("[${dateFormat}][ERROR] %s".format(format))
        else println("[${dateFormat}][ERROR] %s".format(format, *args))
    }

    companion object
    {
        /**
         * 코드가 실행되는 작업 경로를 기준으로 하여 로그를 기록하는 객체입니다.
         * 별도의 디렉토리를 설정하지 않습니다.
         */
        private val DEFAULT_LOG : Log = Log()

        /**
         * 로그 기록 타입을 정의합니다.
         */
        // 기본 정보만 출력
        const val OVERSIGHT_LOG_INFO  : OVERSIGHT_LOG_TYPE = 0

        // 경고 메세지 출력
        const val OVERSIGHT_LOG_WARN  : OVERSIGHT_LOG_TYPE = 1

        // 에러 메시지 출력
        const val OVERSIGHT_LOG_ERROR : OVERSIGHT_LOG_TYPE = 2

        // 디버그 메시지 출력
        const val OVERSIGHT_LOG_DEBUG : OVERSIGHT_LOG_TYPE = 3

        /**
         * 로그를 기록합니다. 해당 로그는 기본 로그 클래스를 호출하여 메시지를 기록합니다.
         */
        fun print(logType : OVERSIGHT_LOG_TYPE, format : String, vararg args : Any) = DEFAULT_LOG.print(logType, format, *args)
    }
}