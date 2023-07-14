
import subprocess
 
p1 = subprocess.Popen(['make', 'start-server'])
p1.terminate()