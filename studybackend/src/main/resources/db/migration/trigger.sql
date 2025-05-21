USE [SWB_DB2_Projekt]
GO
/****** Object:  Trigger [dbo].[trg_mafeit03_createDefaultTaskAfterStudentInsert]    Script Date: 19/05/2025 19:16:43 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<mafeit03>
-- Create date: <19.05.2025>
-- Description:	<Create a welcome task for a new user(student)>
-- =============================================
ALTER TRIGGER [dbo].[trg_mafeit03_createDefaultTaskAfterStudentInsert]
ON [dbo].[mafeit03_students]
AFTER INSERT
AS 
BEGIN

	SET NOCOUNT ON;

	INSERT INTO dbo.mafeit03_tasks (title, description, student_id, is_completed, created_at, updated_at)
	SELECT
		'Welcome to study tracker!',
		'Get familiar with the study tracker and plan your first study session.',
		i.id,
		0,
		GETDATE(),
		GETDATE()
	FROM inserted i;
END;
