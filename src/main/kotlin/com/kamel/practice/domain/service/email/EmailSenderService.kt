package com.kamel.practice.domain.service.email

import org.springframework.core.io.Resource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailSender: JavaMailSender) {

    fun sendEmail(to: String, subject: String, body: String, resource: Resource) {
        // val message = SimpleMailMessage()
        val mimeMessage = emailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, false) // true = HTML content
        helper.addAttachment(resource.filename ?: "", resource)
//        message.setTo(to)
//        message.subject = subject
//        message.text = body
        emailSender.send(mimeMessage)
        //emailSender.send(message)
    }
}