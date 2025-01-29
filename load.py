import shutil
import os

source_dir = r"C:\Users\anton\IdeaProjects\Nebula\target\nebula.jar"
destination_dir = r"C:\Users\anton\Documents\Projekte\Developing\Debug\plugins\nebula.jar"

if os.path.exists(source_dir):
    try:
        os.makedirs(os.path.dirname(destination_dir), exist_ok=True)
        shutil.move(source_dir, destination_dir)
        print("File moved successfully.")
    except Exception as e:
        print(f"An error occurred: {e}")
else:
    print(f"Source file not found: {source_dir}")