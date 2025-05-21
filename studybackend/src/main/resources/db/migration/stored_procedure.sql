-- =============================================
-- Author:		<mafeit03>
-- Create date: <19.05.2025>
-- Description:	<Log all updates regarding a task>
-- =============================================

CREATE PROCEDURE dbo.mafeit03_logTaskAction (
	@p_task_id BIGINT,
	@p_student_id BIGINT,
	@p_action NVARCHAR(255),
	@p_details NVARCHAR(MAX)
)
AS
BEGIN
	SET NOCOUNT ON;

	INSERT INTO dbo.mafeit03_task_logs (task_id, student_id, action, details, log_timestamp)
	VALUES (@p_task_id, @p_student_id, @p_action, @p_details, GETDATE());
END;
GO

