package com.bny.velocity;

import java.io.StringWriter;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class VelocityHelper {

	private static final Logger logger = LoggerFactory.getLogger(VelocityHelper.class.getName());
	
	public static final String EMAIL_CONTENT_TYPE = "text/html";
	
	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private VelocityEngine velocityEngine;
	
	
	public String getTemplateMessage() {

		final StringWriter reportContentWriter = new StringWriter();
		final VelocityEngine velocityEngine = new VelocityEngine();
		Template template = null;
		try {
			final Properties prop = new Properties();
			prop.setProperty(Velocity.RESOURCE_LOADER, "class");
			prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
			prop.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			prop.setProperty("velocity.engine.resource.manager.cache.enabled", "false");
			prop.setProperty("runtime.log.logsystem.log4j.category", "velocity");
			velocityEngine.init(prop);
			template = velocityEngine.getTemplate("email.vm");
		} catch (Exception e) {
			logger.error("Error while initializing velocity engine........", e);
			return e.getMessage();
		}
		final VelocityContext context = new VelocityContext();
		context.put("title", "key1");
		context.put("body", "key2");
		try {
			template.merge(context, reportContentWriter);
		} catch (Exception e) {
			logger.error("error while evaluating/merge the velocity template........", e);
		}
		return reportContentWriter.toString();
	}
	public MimeMessage buildCollectionsEmailMessage(String reportBody) {
		System.setProperty("java.net.preferIPv4Stack", "true");
		final String from = "from";
		final String to = "to";
		final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		
		try {
			final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			
			mimeMessageHelper.setSubject("subject");
			mimeMessageHelper.setFrom(from);
			mimeMessageHelper.setTo(StringUtils.split(to, ","));

			Multipart multipart = new MimeMultipart();
			mimeMessage.setContent(multipart);

			// Message with inline HTML
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(reportBody, EMAIL_CONTENT_TYPE);
			multipart.addBodyPart(messageBodyPart);
			
			
		} catch (MessagingException e) {
			logger.error("Exception while sending mail. ", e);
		}
		return mimeMessage;
	}
	
	public void sendEmail() {
		MimeMessage mimeMessage = null;
		try {
			logger.info("Started sending email report for Variant Collections.........");
			mimeMessage = buildCollectionsEmailMessage(getTemplateMessage());
			javaMailSender.send(mimeMessage);
			logger.info("Variant Collection Email Report Send Succesfully");
		} catch (Exception e) {
			logger.error("Error while sending Variant Collection Email Report", e);
			
		}
	}
}
