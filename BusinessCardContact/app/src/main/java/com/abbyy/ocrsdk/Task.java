package com.abbyy.ocrsdk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Task class to interpret the status of the submitted task when querying the server.
 * Initial version from ABBYY Cloud OCR example. Modified by Alex Pearson.
 */
public class Task {

	/**
	 * Enum of the possible TaskStatus that can be returned
	 */
	public enum TaskStatus {
		Unknown, Submitted, Queued, InProgress, Completed, ProcessingFailed, Deleted, NotEnoughCredits
	}

    /**
     * Constructor to create a new task from the provided reader
     * @param reader Reader to create the Task from
     * @throws Exception If anything goes wrong
     */
	public Task(Reader reader) throws Exception {
		// Read all text into string
		// String data = new Scanner(reader).useDelimiter("\\A").next();
		// Read full task information from xml
		InputSource source = new InputSource();
		source.setCharacterStream(reader);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(source);

		NodeList taskNodes = doc.getElementsByTagName("task");
		Element task = (Element) taskNodes.item(0);

		parseTask(task);
	}

    //Store information about the Task
	public TaskStatus Status = TaskStatus.Unknown;
	public String Id;
	public String DownloadUrl;

    /**
     * Check if the task is active, it's active if it's Queued or InProgress
     * @return if the task is active
     */
	public Boolean isTaskActive() {
		if (Status == TaskStatus.Queued || Status == TaskStatus.InProgress) {
			return true;
		}

		return false;
	}

    /**
     * Parse the task to get the status
     * @param taskElement Element of the Document to check
     */
	private void parseTask(Element taskElement) {
		Id = taskElement.getAttribute("id");
		Status = parseTaskStatus(taskElement.getAttribute("status"));
		if (Status == TaskStatus.Completed) {
			DownloadUrl = taskElement.getAttribute("resultUrl");
		}
	}

    /**
     * Converts the status into the Enum values
     * @param status String representation of the status
     * @return Enum representation of the status
     */
	private TaskStatus parseTaskStatus(String status) {
		if (status.equals("Submitted")) {
			return TaskStatus.Submitted;
		} else if (status.equals("Queued")) {
			return TaskStatus.Queued;
		} else if (status.equals("InProgress")) {
			return TaskStatus.InProgress;
		} else if (status.equals("Completed")) {
			return TaskStatus.Completed;
		} else if (status.equals("ProcessingFailed")) {
			return TaskStatus.ProcessingFailed;
		} else if (status.equals("Deleted")) {
			return TaskStatus.Deleted;
		} else if (status.equals("NotEnoughCredits")) {
			return TaskStatus.NotEnoughCredits;
		} else {
			return TaskStatus.Unknown;
		}
	}

}
