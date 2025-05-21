-- =============================================
-- Author:		<mafeit03>
-- Create date: <19.05.2025>
-- Description:	<Get pending task count for student>
-- =============================================

CREATE FUNCTION dbo.mafeit03_getStudentPendingTaskCount (@p_student_id BIGINT)
RETURNS INT
AS
BEGIN
	DECLARE @task_count INT;

	SELECT @task_count = COUNT(*)
	FROM dbo.mafeit03_tasks
	WHERE student_id = @p_student_id AND is_completed = 0;

	RETURN ISNULL(@task_count, 0);
END;
GO